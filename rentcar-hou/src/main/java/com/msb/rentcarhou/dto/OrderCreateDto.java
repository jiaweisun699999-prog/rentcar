package com.msb.rentcarhou.dto;

import lombok.Data;

/**
 * 订单创建数据传输对象 (Data Transfer Object)
 * 作用：专门用于接收前端在“确认下单”时传给后端的参数集合
 */
@Data // 自动生成 get/set、toString 等方法，保持代码整洁
public class OrderCreateDto {
    /** 用户选中的车型ID (关联 car_model 表) */
    private Long carModelId;
    /** 用户选择的取车门店ID */
    private Long pickupStoreId;
    /** 用户选择的还车门店ID */
    private Long dropoffStoreId;
    /** 用户选择的预计取车时间 (格式: yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm) */
    private String startTime;
    /** 用户选择的预计还车时间 */
    private String endTime;
}
