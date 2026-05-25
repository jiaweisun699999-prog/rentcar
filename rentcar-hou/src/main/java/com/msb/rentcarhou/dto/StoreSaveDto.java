package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class StoreSaveDto {
    private Long id;
    private String merchantName;
    private String cityName;
    private String address;
    private Integer isSupportDelivery;
}
