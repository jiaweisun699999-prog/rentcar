package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.OrderQueryDto;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.vo.OrderDetailVo;
import com.msb.rentcarhou.vo.OrderListVo;
import com.msb.rentcarhou.dto.OrderCreateDto;
import com.msb.rentcarhou.vo.OrderPreviewVo;

/**
 * 订单业务逻辑层接口
 * 作用：声明订单模块对外提供的所有业务能力。
 * 继承 IService<CarOrder> 后，自动拥有了一套通用的增删改查方法，无需重新手写。
 */
public interface CarOrderService extends IService<CarOrder> {
    
    /**
     * 根据查询条件获取带分页的订单列表
     */
    Page<OrderListVo> getOrderPage(OrderQueryDto dto);

    /**
     * 根据订单号查询用于前端渲染的订单详情
     */
    OrderDetailVo getOrderDetail(String orderNo);

    /**
     * 提交下单前，预览算好的各项费用
     */
    OrderPreviewVo previewOrder(OrderCreateDto dto);

    /**
     * 正式执行创建新订单的业务
     * @return 返回生成的订单号
     */
    String createOrder(OrderCreateDto dto);

    /**
     * 模拟用户完成支付操作
     */
    void payOrder(String orderNo);

    /**
     * 更新订单状态（包含连带触发的库存释放等复杂逻辑）
     */
    void updateOrderStatus(String orderNo, Integer status);
}
