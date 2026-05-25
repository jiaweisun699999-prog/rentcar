package com.msb.rentcarhou.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailVo {
    private String orderNo;
    private String brandSeries;
    private String startTime;
    private String endTime;
    private BigDecimal totalAmount;
    private BigDecimal rentFee;
    private BigDecimal basicInsuranceFee;
    private BigDecimal handlingFee;
    private Integer status;
    private String createTime;
    private String pickupLocation;
    private String dropoffLocation;
    private Long carId;
}
