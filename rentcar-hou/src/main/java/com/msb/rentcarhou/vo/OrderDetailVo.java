package com.msb.rentcarhou.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单详情视图对象 (View Object)
 * 作用：用于封装某个订单的详细信息，原封不动地交给前端去展示在“订单详情弹窗”中。
 * 这里面的数据通常是从多张数据库表里联合查询拼凑出来的。
 */
@Data
public class OrderDetailVo {
    /** 系统生成的业务单号 */
    private String orderNo;
    /** 预订的车型名称 (给用户看的中文，而不是干巴巴的 model_id) */
    private String brandSeries;
    /** 格式化好的取车时间字符串 */
    private String startTime;
    /** 格式化好的还车时间字符串 */
    private String endTime;
    /** 客户需要支付的总计金额 (包含各项杂费) */
    private BigDecimal totalAmount;
    /** 纯车辆租金 (每天租金 × 天数) */
    private BigDecimal rentFee;
    /** 基础安全保障费 (每天50元 × 天数) */
    private BigDecimal basicInsuranceFee;
    /** 一次性车辆整备手续费 */
    private BigDecimal handlingFee;
    /** 订单当前的进度状态码 (前端会据此渲染颜色标签) */
    private Integer status;
    /** 下单时间 */
    private String createTime;
    /** 取车地点的中文详细地址 (通过 store_id 关联查出来的) */
    private String pickupLocation;
    /** 还车地点的中文详细地址 */
    private String dropoffLocation;
    /** 具体被分配到的那辆车的ID (后台排查问题用) */
    private Long carId;
}
