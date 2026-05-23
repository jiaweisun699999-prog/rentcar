package com.msb.rentcarhou.dto;

import lombok.Data;

@Data
public class RegisterReqDto {
    private String phone;
    private String password;
    private String smsCode;
}
