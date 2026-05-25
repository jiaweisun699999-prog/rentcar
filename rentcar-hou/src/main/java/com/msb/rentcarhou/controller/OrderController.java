package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.service.CarOrderService;
import com.msb.rentcarhou.vo.OrderListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderController {

    @Autowired
    private CarOrderService carOrderService;

    @GetMapping("/order/list")
    public Result<Page<OrderListVo>> list(OrderQueryDto dto) {
        Page<OrderListVo> page = carOrderService.getOrderPage(dto);
        return Result.success(page);
    }

    @PostMapping("/pay/mock")
    public Result<String> payMock(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error("订单号不能为空");
        }
        carOrderService.payOrder(orderNo);
        return Result.success("支付成功", null);
    }
}
