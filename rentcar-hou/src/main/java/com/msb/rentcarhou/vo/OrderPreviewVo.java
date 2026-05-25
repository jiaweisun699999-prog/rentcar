package com.msb.rentcarhou.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderPreviewVo {
    private Integer rentDays;
    private BigDecimal rentFee;
    private BigDecimal basicInsuranceFee;
    private BigDecimal handlingFee;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
}
