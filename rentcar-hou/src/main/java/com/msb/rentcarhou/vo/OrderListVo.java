package com.msb.rentcarhou.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderListVo {
    private String orderId;
    private Long userId;
    private String brandSeries;
    private String startDate;
    private String endDate;
    private BigDecimal totalAmount;
    private Integer status;
    private String createTime;
}
