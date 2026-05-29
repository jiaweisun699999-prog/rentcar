package com.msb.rentcarhou.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单列表视图对象 (View Object)
 * 作用：为了不让列表页加载过慢，列表卡片上只展示最核心的信息。
 * 这个类把数据库里复杂的字段精简后返回给前端的 Table 渲染。
 */
@Data
public class OrderListVo {
    /** 业务单号 */
    private String orderId;
    /** 下单用户的ID */
    private Long userId;
    /** 预订的车型名称概要 */
    private String brandSeries;
    /** 开始租期 */
    private String startDate;
    /** 结束租期 */
    private String endDate;
    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 状态码 */
    private Integer status;
    /** 创建时间 */
    private String createTime;
}
