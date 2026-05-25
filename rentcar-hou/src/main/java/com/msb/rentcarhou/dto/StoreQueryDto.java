package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class StoreQueryDto {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String cityName;
}
