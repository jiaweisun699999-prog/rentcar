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

@Service
public class CarOrderServiceImpl extends ServiceImpl<CarOrderMapper, CarOrder> implements CarOrderService {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Page<OrderListVo> getOrderPage(OrderQueryDto dto) {
        Long currentUserId = com.msb.rentcarhou.common.utils.UserContext.getUserId();
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        if (currentUserId != null) {
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

    @Override
    public void payOrder(String orderNo) {
        LambdaUpdateWrapper<CarOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CarOrder::getOrderNo, orderNo)
                .set(CarOrder::getStatus, 1);
        this.update(wrapper);
    }

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

    @Override
    public OrderDetailVo getOrderDetail(String orderNo) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarOrder::getOrderNo, orderNo);
        Long currentUserId = com.msb.rentcarhou.common.utils.UserContext.getUserId();
        if (currentUserId != null) {
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
