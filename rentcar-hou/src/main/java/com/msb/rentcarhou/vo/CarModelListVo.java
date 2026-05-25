package com.msb.rentcarhou.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CarModelListVo {
    private Long id;
    private String brandSeries;
    private String carType;
    private String seatsDoors;
    private String mainImage;
    private BigDecimal dailyPrice;
    private String licensePlate;
    private String locationCity;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
