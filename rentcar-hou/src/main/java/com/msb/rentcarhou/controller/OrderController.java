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

/**
 * 订单相关的控制器 (Controller)
 * 负责接收前端的HTTP请求，调用Service层处理业务逻辑，并向前端返回统一格式的响应结果
 */
@RestController // 标识这是一个RESTful风格的控制器，所有方法的返回值会自动被转换为JSON格式
@RequestMapping("/api") // 设置该类中所有接口的基础路径为 "/api"
@CrossOrigin // 允许跨域请求（前端Vue项目和后端Spring Boot项目端口不同时需要）
public class OrderController {

    @Autowired // 自动注入订单的业务逻辑处理类 (Service层)
    private CarOrderService carOrderService;

    /**
     * 获取订单列表接口
     * @param dto 封装了前端传来的分页参数(page, pageSize)和查询条件
     * @return 包含分页订单数据的统一返回结果
     */
    @GetMapping("/order/list")
    public Result<Page<OrderListVo>> list(OrderQueryDto dto) {
        // 调用Service层查询分页数据
        Page<OrderListVo> page = carOrderService.getOrderPage(dto);
        // 使用 Result.success 将数据包裹并返回给前端
        return Result.success(page);
    }

    /**
     * 模拟支付接口
     * @param params 接收前端传来的JSON参数（包含 orderNo 等）
     * @return 支付结果提示
     */
    @PostMapping("/pay/mock")
    public Result<String> payMock(@RequestBody Map<String, Object> params) {
        // 从请求体中提取订单号
        String orderNo = (String) params.get("orderNo");
        // 校验订单号是否为空
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error("订单号不能为空");
        }
        // 调用Service层的支付逻辑，更新订单状态
        carOrderService.payOrder(orderNo);
        return Result.success("支付成功", null);
    }

    /**
     * 获取订单详情接口
     * @param orderNo 订单号（通过URL的查询参数传递，例如 ?orderNo=xxx）
     * @return 订单详情数据
     */
    @GetMapping("/order/detail")
    public Result<OrderDetailVo> detail(@RequestParam("orderNo") String orderNo) {
        // 调用Service层查询订单详细信息
        OrderDetailVo detail = carOrderService.getOrderDetail(orderNo);
        // 如果查不到数据，返回错误提示
        if (detail == null) {
            return Result.error("订单不存在或无权查看");
        }
        return Result.success(detail);
    }

    /**
     * 订单预览接口（在用户确认下单前，展示各项费用明细）
     * @param dto 封装了前端传来的下单预览参数（如车型ID、取还车时间地点等）
     * @return 订单费用预览数据
     */
    @PostMapping("/order/preview")
    public Result<OrderPreviewVo> preview(@RequestBody OrderCreateDto dto) {
        // 调用Service层计算各项费用，并返回预览视图对象
        return Result.success(carOrderService.previewOrder(dto));
    }

    /**
     * 创建订单（提交订单）接口
     * @param dto 封装了前端传来的下单参数
     * @return 创建成功后的订单号
     */
    @PostMapping("/order/create")
    public Result<String> create(@RequestBody OrderCreateDto dto) {
        try {
            // 调用Service层执行创建订单的复杂业务逻辑
            String orderNo = carOrderService.createOrder(dto);
            // 成功则返回新生成的订单号
            return Result.success(orderNo);
        } catch (Exception e) {
            // 捕获业务异常并返回给前端（例如库存不足、时间冲突等）
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单状态接口（如：前端点击“还车”时调用）
     * @param dto 封装了需要更新的订单号和目标状态码
     * @return 操作结果
     */
    @PutMapping("/order/status/update")
    public Result<Void> updateStatus(@RequestBody com.msb.rentcarhou.dto.OrderStatusUpdateDto dto) {
        try {
            // 调用Service层更新指定订单的状态
            carOrderService.updateOrderStatus(dto.getOrderNo(), dto.getStatus());
            // 成功返回（没有额外数据，所以是 null）
            return Result.success(null);
        } catch (Exception e) {
            // 失败则返回异常信息
            return Result.error(e.getMessage());
        }
    }
}
