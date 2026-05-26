package com.msb.rentcarhou.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarInstanceReqDto {
    private Long modelId;
    private Long storeId;
    private String plateNumber;
    private BigDecimal dailyRentPrice;
}
