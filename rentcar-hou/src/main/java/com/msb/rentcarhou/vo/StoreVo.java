package com.msb.rentcarhou.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class StoreVo {
    private Long id;
    private Long merchantId;
    private String merchantName;
    private BigDecimal serviceScore;
    private String cityName;
    private String address;
    private Integer isSupportDelivery;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
