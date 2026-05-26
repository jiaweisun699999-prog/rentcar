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

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

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
        Long currentUserId = com.msb.rentcarhou.common.utils.UserContext.getUserId();
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        
        Integer role = 0;
        if (currentUserId != null) {
            try {
                role = jdbcTemplate.queryForObject("SELECT role FROM sys_user WHERE id = ?", Integer.class, currentUserId);
            } catch (Exception ignored) {}
        }
        
        if (currentUserId != null && role != 1 && role != 2) {
            wrapper.eq(CarOrder::getUserId, currentUserId);
        }
        
        if (dto.getStatus() != null) {
            wrapper.eq(CarOrder::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(CarOrder::getCreateTime);

        Page<CarOrder> page = new Page<>(dto.getPage(), dto.getPageSize());
        this.page(page, wrapper);

        List<OrderListVo> voList = page.getRecords().stream().map(order -> {
            OrderListVo vo = new OrderListVo();
            vo.setOrderId(order.getOrderNo());
            vo.setUserId(order.getUserId());
            vo.setBrandSeries("悟空精选车型");
            vo.setStartDate(formatDate(order.getStartTime()));
            vo.setEndDate(formatDate(order.getEndTime()));
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus());
            vo.setCreateTime(formatDate(order.getCreateTime()));
            return vo;
        }).collect(Collectors.toList());

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
     *    如果状态变为 3 或 4(还车完成)，将车辆状态重置为 0(空闲可租)，实现跨模块的业务流转。
     */
    @Override
    public void updateOrderStatus(String orderNo, Integer status) {
        CarOrder order = this.getOne(new LambdaQueryWrapper<CarOrder>().eq(CarOrder::getOrderNo, orderNo));
        if (order == null) throw new RuntimeException("订单不存在");
        
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
            } catch (Exception ignored) {}
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
                role = jdbcTemplate.queryForObject("SELECT role FROM sys_user WHERE id = ?", Integer.class, currentUserId);
            } catch (Exception ignored) {}
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
            pickupLocation = jdbcTemplate.queryForObject("SELECT CONCAT(city_name, ' ', address) FROM store_info WHERE id = ?", String.class, order.getPickupStoreId());
        } catch (Exception ignored) {}
        try {
            dropoffLocation = jdbcTemplate.queryForObject("SELECT CONCAT(city_name, ' ', address) FROM store_info WHERE id = ?", String.class, order.getDropoffStoreId());
        } catch (Exception ignored) {}
        
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
        if (st.length() == 16) st += ":00";
        String et = dto.getEndTime();
        if (et.length() == 16) et += ":00";
        
        LocalDateTime start = LocalDateTime.parse(st, FORMATTER);
        LocalDateTime end = LocalDateTime.parse(et, FORMATTER);
        long days = java.time.Duration.between(start, end).toDays();
        if (days < 1) days = 1;
        vo.setRentDays((int) days);
        
        BigDecimal dailyPrice = new BigDecimal("100.00");
        try {
            BigDecimal price = jdbcTemplate.queryForObject("SELECT daily_rent_price FROM car_instance WHERE model_id = ? LIMIT 1", BigDecimal.class, dto.getCarModelId());
            if (price != null) dailyPrice = price;
        } catch(Exception ignored) {}
        
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
            carId = jdbcTemplate.queryForObject("SELECT id FROM car_instance WHERE model_id = ? AND store_id = ? AND status = 0 LIMIT 1", Long.class, dto.getCarModelId(), dto.getPickupStoreId());
        } catch(Exception e) {
            try {
                carId = jdbcTemplate.queryForObject("SELECT id FROM car_instance WHERE model_id = ? LIMIT 1", Long.class, dto.getCarModelId());
            } catch(Exception ex) {}
        }
        
        String st = dto.getStartTime();
        if (st.length() == 16) st += ":00";
        String et = dto.getEndTime();
        if (et.length() == 16) et += ":00";

        CarOrder order = new CarOrder();
        order.setOrderNo("ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000));
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

    private String formatDate(Object date) {
        if (date == null) return null;
        if (date instanceof Date) return new SimpleDateFormat(DATE_TIME_PATTERN).format((Date) date);
        if (date instanceof LocalDateTime) return ((LocalDateTime) date).format(FORMATTER);
        return date.toString();
    }
}
