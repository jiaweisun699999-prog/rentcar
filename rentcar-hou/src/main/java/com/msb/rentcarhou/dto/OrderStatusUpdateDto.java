package com.msb.rentcarhou.dto;

import lombok.Data;

/**
 * 订单状态更新数据传输对象
 * 作用：接收前端发起的“修改订单状态”请求参数，比如点击“还车”、“取消订单”等按钮时触发
 */
@Data
public class OrderStatusUpdateDto {
    /** 要操作的系统业务订单号 (例如: ORD123456789) */
    private String orderNo;
    /** 想要把订单改成的目标状态码 (如 3代表待结算) */
    private Integer status;
}
