package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class FinanceQueryDto {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String startDate;
    private String endDate;
}
