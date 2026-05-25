package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class OrderStatusUpdateDto {
    private String orderNo;
    private Integer status;
}
