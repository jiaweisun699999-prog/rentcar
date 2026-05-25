package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.vo.OrderDetailVo;
import com.msb.rentcarhou.vo.OrderListVo;

public interface CarOrderService extends IService<CarOrder> {
    Page<OrderListVo> getOrderPage(OrderQueryDto dto);

    OrderDetailVo getOrderDetail(String orderNo);

    void payOrder(String orderNo);
}
