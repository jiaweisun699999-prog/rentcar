package com.msb.rentcarhou.vo;

import lombok.Data;

@Data
public class UserCreditInfoVo {
    private Integer creditScore;
    private Boolean isDepositWaived;
    private String idCardNo;
    private String driverLicenseUrl;
    private Integer auditStatus; // 1-审核中, 2-通过, 3-驳回
}
