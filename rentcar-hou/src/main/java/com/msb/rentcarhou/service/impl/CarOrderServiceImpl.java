package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.mapper.CarOrderMapper;
import com.msb.rentcarhou.service.CarOrderService;
import com.msb.rentcarhou.vo.OrderDetailVo;
import com.msb.rentcarhou.vo.OrderListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarOrderServiceImpl extends ServiceImpl<CarOrderMapper, CarOrder> implements CarOrderService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            vo.setStartDate(order.getStartTime() != null ? order.getStartTime().format(FORMATTER) : null);
            vo.setEndDate(order.getEndTime() != null ? order.getEndTime().format(FORMATTER) : null);
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus());
            vo.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().format(FORMATTER) : null);
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
        vo.setStartTime(order.getStartTime() != null ? order.getStartTime().format(FORMATTER) : null);
        vo.setEndTime(order.getEndTime() != null ? order.getEndTime().format(FORMATTER) : null);
        vo.setTotalAmount(order.getTotalAmount());
        vo.setRentFee(order.getRentFee());
        vo.setBasicInsuranceFee(order.getBasicInsuranceFee());
        vo.setHandlingFee(order.getHandlingFee());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().format(FORMATTER) : null);
        
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
}
