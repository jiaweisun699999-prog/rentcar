package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class OrderQueryDto {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer status;
}
