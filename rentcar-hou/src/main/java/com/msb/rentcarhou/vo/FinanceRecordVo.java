package com.msb.rentcarhou.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class FinanceRecordVo {
    private String transactionId;
    private String orderId;
    private BigDecimal amount;
    private Integer type;
    private Integer tradeType;
    private String tradeTypeName;
    private Integer status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
