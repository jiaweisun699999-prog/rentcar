package com.msb.rentcarhou.vo;

import lombok.Data;

@Data
public class UserInfoVo {
    private Long id;
    private String phone;
    private Integer role;
    private Integer creditScore;
}
