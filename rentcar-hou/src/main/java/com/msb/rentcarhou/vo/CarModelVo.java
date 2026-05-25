package com.msb.rentcarhou.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CarModelVo {
    private Long id;
    private String brandSeries;
    private String carType;
    private String seatsDoors;
    private String mainImage;
    private BigDecimal dailyPrice;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
