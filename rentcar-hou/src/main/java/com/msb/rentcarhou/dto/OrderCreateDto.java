package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class OrderCreateDto {
    private Long carModelId;
    private Long pickupStoreId;
    private Long dropoffStoreId;
    private String startTime;
    private String endTime;
}
