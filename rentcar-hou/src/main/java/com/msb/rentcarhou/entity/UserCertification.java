package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_certification")
public class UserCertification {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    
    private String idCardNo;
    private String driverLicenseUrl;
    private Integer auditStatus; // 1-审核中, 2-通过, 3-驳回
}
