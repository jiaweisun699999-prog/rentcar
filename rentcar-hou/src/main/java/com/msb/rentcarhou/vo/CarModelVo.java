package com.msb.rentcarhou.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarModelVo {
    private Long id;
    private String brandSeries;
    private String carType;
    private String seatsDoors;
    private String mainImage;
    private BigDecimal dailyPrice;
    private String licensePlate;
    private String locationCity;
    private Long storeId;
}
