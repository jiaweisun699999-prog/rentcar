package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.service.CarOrderService;
import com.msb.rentcarhou.vo.OrderDetailVo;
import com.msb.rentcarhou.vo.OrderListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.msb.rentcarhou.dto.OrderCreateDto;
import com.msb.rentcarhou.vo.OrderPreviewVo;

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

    @GetMapping("/order/detail")
    public Result<OrderDetailVo> detail(@RequestParam("orderNo") String orderNo) {
        OrderDetailVo detail = carOrderService.getOrderDetail(orderNo);
        if (detail == null) {
            return Result.error("订单不存在或无权查看");
        }
        return Result.success(detail);
    }

    @PostMapping("/order/preview")
    public Result<OrderPreviewVo> preview(@RequestBody OrderCreateDto dto) {
        return Result.success(carOrderService.previewOrder(dto));
    }

    @PostMapping("/order/create")
    public Result<String> create(@RequestBody OrderCreateDto dto) {
        try {
            String orderNo = carOrderService.createOrder(dto);
            return Result.success(orderNo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/order/status/update")
    public Result<Void> updateStatus(@RequestBody com.msb.rentcarhou.dto.OrderStatusUpdateDto dto) {
        try {
            carOrderService.updateOrderStatus(dto.getOrderNo(), dto.getStatus());
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
