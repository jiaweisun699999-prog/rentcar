package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.mapper.CarOrderMapper;
import com.msb.rentcarhou.service.CarOrderService;
import com.msb.rentcarhou.vo.OrderListVo;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarOrderServiceImpl extends ServiceImpl<CarOrderMapper, CarOrder> implements CarOrderService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<OrderListVo> getOrderPage(OrderQueryDto dto) {
        LambdaQueryWrapper<CarOrder> wrapper = new LambdaQueryWrapper<>();
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
}
