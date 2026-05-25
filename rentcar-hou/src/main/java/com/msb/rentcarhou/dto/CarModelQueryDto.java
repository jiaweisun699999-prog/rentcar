package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class CarModelQueryDto {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Long storeId;
    private String keyword;
    private String startTime;
    private String endTime;
}
