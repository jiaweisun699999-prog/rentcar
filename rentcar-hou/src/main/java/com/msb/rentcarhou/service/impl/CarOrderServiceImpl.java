package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.OrderCreateDto;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.mapper.CarOrderMapper;
import com.msb.rentcarhou.service.CarOrderService;
import com.msb.rentcarhou.vo.OrderDetailVo;
import com.msb.rentcarhou.vo.OrderListVo;
import com.msb.rentcarhou.vo.OrderPreviewVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务的具体实现类 (Service层)
 * 核心业务逻辑的大脑。负责接收 Controller 传来的 DTO 数据，处理复杂的业务规则（如计算价格、校验状态），
 * 调用 Mapper 与数据库交互，并将原始数据拼装成前端所需的 VO 对象返回。
 */
@Service
public class CarOrderServiceImpl extends ServiceImpl<CarOrderMapper, CarOrder> implements CarOrderService {
    // 定义日期格式常量
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    // 创建线程安全的日期格式化器
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    // 注入原生的 SQL 执行工具
    /**
     * @Autowired: 这是 Spring 框架的核心注解，叫做“依赖注入”。它的作用是告诉 Spring 容器：“我在运行这个类的时候需要一个
     *             JdbcTemplate 对象，请你帮我自动找一个并赋值给这个变量”，这样您就不需要自己去 new 它了。
     *             JdbcTemplate: 这是 Spring 提供的一个操作数据库的底层工具类。虽然您的项目里已经用了
     *             MyBatis-Plus（ServiceImpl 泛型），但在某些复杂的场景下（例如极其复杂的多表联查、存储过程调用，或者不想写
     *             Mapper 的临时原生 SQL 操作），使用 JdbcTemplate 直接执行写好的 SQL
     *             字符串会更加灵活和方便。在这里把它引进来，是为了给类内部可能出现的复杂原生 SQL 操作做准备。
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 【1. 查列表】获取订单分页数据
     * 闭环逻辑：
     * 1. 接收 Controller 透传的分页和查询 DTO。
     * 2. 获取当前登录用户，如果是普通用户只能查自己的，管理员可以查所有。
     * 3. 调用 MyBatis-Plus 提供的 this.page() 从 car_order 表拉取分页数据。
     * 4. 遍历实体类 CarOrder，转换为前端所需的 OrderListVo（如格式化时间）。
     * 5. 返回组装好的 Page<OrderListVo> 给 Controller。
     */
    @Override
    public Page<OrderListVo> getOrderPage(OrderQueryDto dto) {

        /**
         * currentUserId：通过系统上下文（通常是从前端传来的 Token 中解析出来的），获取当前正在发请求的用户的 ID。
         * LambdaQueryWrapper：这是 MyBatis-Plus 提供的“拼接器”，我们后面所有的 WHERE 查询条件，都要往这wrapper
         * 里面塞。
         */
        Long currentUserId = com.msb.rentcarhou.common.utils.UserContext.getUserId();
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        // 去系统里查一下他的“真实身份” (权限级别)
        /**
         * 系统拿到用户 ID 后，写了一条底层的原生 SQL 去 sys_user（用户表）里查这个人的 role（角色）。
         * 如果查到了，role 会被赋上相应的值（比如普通用户可能是 0，门店管理员是 1，超级管理员是 2）。
         */
        Integer role = 0;
        if (currentUserId != null) {
            try {
                role = jdbcTemplate.queryForObject("SELECT role FROM sys_user WHERE id = ?", Integer.class,
                        currentUserId);
            } catch (Exception ignored) {
            }
        }
        // 3. 数据隔离
        /**
         * 作用是防止普通用户偷窥别人的订单（防止越权）
         * 它的逻辑是：如果这个人是普通用户（即 role 既不是 1 也不是 2），那么我强行给 SQL 加上一个条件：WHERE user_id =
         * 当前用户ID。
         * 这样一来，底层查数据库时，永远只会返回属于他自己的那几条订单。
         * 反之，如果他是管理员（role == 1 或 2），这个 if 进不去，就不会加上 user_id
         * 的限制，那么底层就会把全库所有人的订单都查出来供他管理。
         */
        if (currentUserId != null && role != 1 && role != 2) {
            wrapper.eq(CarOrder::getUserId, currentUserId);
        }
        /**
         * 4. 加上用户自己在页面上点的过滤条件
         * 前端页面上通常会有“全部”、“待支付”、“租赁中”等分类 Tab。
         * 如果用户点了一下“待支付”，前端就会传个 status=0 过来。
         * 这里就是判断前端有没有传状态过来。如果有传，就在 SQL 后面再接上 AND status = 0。
         */
        if (dto.getStatus() != null) {
            wrapper.eq(CarOrder::getStatus, dto.getStatus());
        }
        /**
         * 5. 排序（让最新的在最上面）
         * 相当于加上了 ORDER BY create_time DESC。
         * 保证返回给前端的列表里，最近刚刚下的新订单排在最前面，几年前的老订单沉在底下。
         */
        wrapper.orderByDesc(CarOrder::getCreateTime);

        /**
         * 执行数据库分页查询
         * new Page<>(...): 根据前端传来的参数（当前页码 dto.getPage()
         * 和每页数量dto.getPageSize()）创建一个分页对象。
         * 
         * this.page(page, wrapper): 这是 MyBatis-Plus（或类似框架）提供的方法。它会根据 wrapper 中拼接的查询条件，
         * 去数据库里查出对应页的数据，并将查询结果（数据列表、总记录数等）填充到 page 对象中。
         * 此时，page.getRecords() 里装的是数据库原生实体类 CarOrder 的集合。
         */
        Page<CarOrder> page = new Page<>(dto.getPage(), dto.getPageSize());
        this.page(page, wrapper);

        /**
         * 将数据实体 (Entity) 转换为视图对象 (VO)
         * 这段代码使用了 Java 8 的 Stream API。
         * page.getRecords().stream(): 获取刚才查出来的 CarOrder 列表，并转换为流（Stream）。
         * .map(order -> { ... }): 遍历列表中的每一个 CarOrder（数据库实体），将其转换为一个新的
         * OrderListVo（专门给前端展示用的对象）。在这个大括号里，把订单号、用户ID、金额、状态等信息一一对应地塞进 vo 里。
         * 其中时间字段调用了 formatDate 方法进行了格式化，并且 BrandSeries（品牌车系）被硬编码成了 "悟空精选车型"。
         * .collect(Collectors.toList()): 将转换后的所有 OrderListVo 重新收集打包成一个新的 List（即
         * voList）。
         * 
         */
        List<OrderListVo> voList = page.getRecords().stream().map(order -> {
            OrderListVo vo = new OrderListVo();
            vo.setOrderId(order.getOrderNo());// 订单号
            vo.setUserId(order.getUserId());// 用户ID
            vo.setBrandSeries("悟空精选车型");
            vo.setStartDate(formatDate(order.getStartTime()));// 开始时间
            vo.setEndDate(formatDate(order.getEndTime()));// 结束时间
            vo.setTotalAmount(order.getTotalAmount());// 总金额
            vo.setStatus(order.getStatus());// 订单状态
            vo.setCreateTime(formatDate(order.getCreateTime()));// 创建时间
            return vo;
        }).collect(Collectors.toList());

        /**
         * 封装并返回最终的分页结果
         * 因为原来的 page 对象泛型是 <CarOrder>，而现在我们需要返回给前端的是 <OrderListVo>，所以不能直接返回原来的 page。
         * new Page<>(...): 创建一个新的分页对象 voPage，并把之前查出来的分页元数据
         * （当前页码 getCurrent()、每页条数 getSize()、总条数 getTotal()）原封不动地复制过来。
         * voPage.setRecords(voList): 把刚才转换好的、处理过的数据列表 voList 塞进这个新的分页对象中。
         * 最后返回这个包裹着 OrderListVo 的分页对象，供前端调用和展示。
         */
        Page<OrderListVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 【2. 去支付】模拟支付并更新状态
     * 闭环逻辑：
     * 1. 接收订单号。
     * 2. 构建 LambdaUpdateWrapper，将对应订单的 status 字段更新为 1 (已支付/待取车)。
     * 3. 调用 this.update() 执行更新。前端在收到 Controller 成功返回后会刷新列表。
     */
    @Override
    public void payOrder(String orderNo) {
        LambdaUpdateWrapper<CarOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CarOrder::getOrderNo, orderNo)
                .set(CarOrder::getStatus, 1);
        this.update(wrapper);
    }

    /**
     * 【4. 更新状态 (还车/取消)】
     * 闭环逻辑：
     * 1. 接收 Controller 传来的订单号和目标状态(如 3=待还车结算)。
     * 2. 查出订单实体，更新状态并保存。
     * 3. 联动车辆库存管理：如果状态变为 2(租赁中)，把车辆设为不可租；
     * 如果状态变为 3 或 4(还车完成)，将车辆状态重置为 0(空闲可租)，实现跨模块的业务流转。
     */
    @Override
    public void updateOrderStatus(String orderNo, Integer status) {
        CarOrder order = this.getOne(new LambdaQueryWrapper<CarOrder>().eq(CarOrder::getOrderNo, orderNo));
        if (order == null)
            throw new RuntimeException("订单不存在");

        order.setStatus(status);
        this.updateById(order);

        // 同步更新车辆库存状态 (不改动其他模块的 Java 代码，直接用 SQL 跨模块操作)
        if (order.getCarId() != null) {
            try {
                if (status == 2) {
                    jdbcTemplate.update("UPDATE car_instance SET status = 2 WHERE id = ?", order.getCarId());
                } else if (status == 3 || status == 4) {
                    jdbcTemplate.update("UPDATE car_instance SET status = 0 WHERE id = ?", order.getCarId());
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 【3. 查详情】获取订单详细明细
     * 闭环逻辑：
     * 1. 根据订单号查询 CarOrder。包含权限校验（只能看自己的）。
     * 2. 将原始 entity 数据塞入 OrderDetailVo 中。
     * 3. 关联查询（跨表）：利用 JdbcTemplate 去 store_info 表中查出取车和还车门店的具体中文地址。
     * 4. 返回拼接完美的 Vo 对象给 Controller 输出 JSON。
     */
    @Override
    public OrderDetailVo getOrderDetail(String orderNo) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarOrder::getOrderNo, orderNo);
        Long currentUserId = com.msb.rentcarhou.common.utils.UserContext.getUserId();

        Integer role = 0;
        if (currentUserId != null) {
            try {
                role = jdbcTemplate.queryForObject("SELECT role FROM sys_user WHERE id = ?", Integer.class,
                        currentUserId);
            } catch (Exception ignored) {
            }
        }

        if (currentUserId != null && role != 1 && role != 2) {
            wrapper.eq(CarOrder::getUserId, currentUserId);
        }

        CarOrder order = this.getOne(wrapper);
        if (order == null) {
            return null;
        }
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrderNo(order.getOrderNo());
        vo.setBrandSeries("悟空精选车型");
        vo.setStartTime(formatDate(order.getStartTime()));
        vo.setEndTime(formatDate(order.getEndTime()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setRentFee(order.getRentFee());
        vo.setBasicInsuranceFee(order.getBasicInsuranceFee());
        vo.setHandlingFee(order.getHandlingFee());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(formatDate(order.getCreateTime()));

        String pickupLocation = "未知";
        String dropoffLocation = "未知";
        try {
            pickupLocation = jdbcTemplate.queryForObject(
                    "SELECT CONCAT(city_name, ' ', address) FROM store_info WHERE id = ?", String.class,
                    order.getPickupStoreId());
        } catch (Exception ignored) {
        }
        try {
            dropoffLocation = jdbcTemplate.queryForObject(
                    "SELECT CONCAT(city_name, ' ', address) FROM store_info WHERE id = ?", String.class,
                    order.getDropoffStoreId());
        } catch (Exception ignored) {
        }

        vo.setPickupLocation(pickupLocation);
        vo.setDropoffLocation(dropoffLocation);
        vo.setCarId(order.getCarId());
        return vo;
    }

    /**
     * 【5. 下单预览】在真正生成订单前，为前端计算费用明细
     * 闭环逻辑：
     * 1. 接收前端选好的租车时间、车型 DTO。
     * 2. 计算租期天数 (不足1天按1天算)。
     * 3. 查库获取该车型的日租金，计算 车辆租金 = 日租金 * 天数。
     * 4. 叠加 基础保障费 (50/天) 和 手续费 (固定20)。
     * 5. 将计算结果封装进 OrderPreviewVo 返回，前端将其展示在“账单确认”弹窗中。
     */
    @Override
    public OrderPreviewVo previewOrder(OrderCreateDto dto) {
        OrderPreviewVo vo = new OrderPreviewVo();
        String st = dto.getStartTime();
        if (st.length() == 16)
            st += ":00";
        String et = dto.getEndTime();
        if (et.length() == 16)
            et += ":00";

        LocalDateTime start = LocalDateTime.parse(st, FORMATTER);
        LocalDateTime end = LocalDateTime.parse(et, FORMATTER);
        long days = java.time.Duration.between(start, end).toDays();
        if (days < 1)
            days = 1;
        vo.setRentDays((int) days);

        BigDecimal dailyPrice = new BigDecimal("100.00");
        try {
            BigDecimal price = jdbcTemplate.queryForObject(
                    "SELECT daily_rent_price FROM car_instance WHERE model_id = ? LIMIT 1", BigDecimal.class,
                    dto.getCarModelId());
            if (price != null)
                dailyPrice = price;
        } catch (Exception ignored) {
        }

        BigDecimal rentFee = dailyPrice.multiply(BigDecimal.valueOf(days));
        BigDecimal basicInsuranceFee = new BigDecimal("50.00").multiply(BigDecimal.valueOf(days));
        BigDecimal handlingFee = new BigDecimal("20.00");
        BigDecimal total = rentFee.add(basicInsuranceFee).add(handlingFee);

        vo.setRentFee(rentFee);
        vo.setBasicInsuranceFee(basicInsuranceFee);
        vo.setHandlingFee(handlingFee);
        vo.setTotalAmount(total);
        vo.setDepositAmount(BigDecimal.ZERO); // 信用免押

        return vo;
    }

    /**
     * 【6. 创建订单】提交订单入库
     * 闭环逻辑：
     * 1. 获取登录用户。调用预览方法复用价格计算逻辑。
     * 2. 核心：在库存表(car_instance)中寻找一台符合【指定车型 + 指定门店 + 空闲(status=0)】的具体车辆并锁定其ID。
     * 3. 生成全局唯一的订单号 (时间戳+随机数)。
     * 4. 组装 CarOrder 实体并调用 this.save() 插入数据库。
     * 5. 返回生成的订单号给前端，以便前端跳转到支付页。
     */
    @Override
    public String createOrder(OrderCreateDto dto) {
        Long userId = com.msb.rentcarhou.common.utils.UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        OrderPreviewVo preview = previewOrder(dto);

        Long carId = 1L;
        try {
            carId = jdbcTemplate.queryForObject(
                    "SELECT id FROM car_instance WHERE model_id = ? AND store_id = ? AND status = 0 LIMIT 1",
                    Long.class, dto.getCarModelId(), dto.getPickupStoreId());
        } catch (Exception e) {
            try {
                carId = jdbcTemplate.queryForObject("SELECT id FROM car_instance WHERE model_id = ? LIMIT 1",
                        Long.class, dto.getCarModelId());
            } catch (Exception ex) {
            }
        }

        String st = dto.getStartTime();
        if (st.length() == 16)
            st += ":00";
        String et = dto.getEndTime();
        if (et.length() == 16)
            et += ":00";

        CarOrder order = new CarOrder();
        order.setOrderNo("ORD" + System.currentTimeMillis() + (int) (Math.random() * 1000));
        order.setUserId(userId);
        order.setCarId(carId);
        order.setPickupStoreId(dto.getPickupStoreId());
        order.setDropoffStoreId(dto.getDropoffStoreId());
        order.setStartTime(java.sql.Timestamp.valueOf(LocalDateTime.parse(st, FORMATTER)));
        order.setEndTime(java.sql.Timestamp.valueOf(LocalDateTime.parse(et, FORMATTER)));
        order.setTotalAmount(preview.getTotalAmount());
        order.setRentFee(preview.getRentFee());
        order.setBasicInsuranceFee(preview.getBasicInsuranceFee());
        order.setHandlingFee(preview.getHandlingFee());
        order.setStatus(0); // 待支付
        order.setCreateTime(new Date());

        this.save(order);
        return order.getOrderNo();
    }

    /**
     * 辅助工具方法：将数据库查出来的各种格式的时间对象，统一转换成前端展示需要的“年-月-日 时:分:秒”格式的字符串。
     * 
     * @param date 传入的时间对象（使用 Object 类型是为了兼容新老版本的时间类）
     * @return 格式化后的时间字符串
     */
    private String formatDate(Object date) {
        // 1. 安全防御：如果传进来的时间是空的（比如订单未结束没有结束时间），直接返回 null 防止空指针异常
        if (date == null)
            return null;
        // 2. 兼容老版本：如果传进来的是老版本 Java 的 java.util.Date 类型
        if (date instanceof Date)
            // 临时创建一个 SimpleDateFormat 工具，把时间强转后格式化成字符串返回
            return new SimpleDateFormat(DATE_TIME_PATTERN).format((Date) date);
        // 3. 兼容新版本：如果传进来的是 Java 8 之后推崇的新版 java.time.LocalDateTime 类型
        if (date instanceof LocalDateTime)
            // 直接调用其自带的 format 方法，传入全局唯一且线程安全的静态常量 FORMATTER 进行格式化
            return ((LocalDateTime) date).format(FORMATTER);
        // 4. 兜底策略：如果既不是 Date 也不是 LocalDateTime（比如是个数字时间戳），强行转成字符串返回，防止程序崩溃
        return date.toString();
    }
}
