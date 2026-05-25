package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class StoreQueryDto {
    private Integer page;
    private Integer pageSize;
    private String cityName;
}
