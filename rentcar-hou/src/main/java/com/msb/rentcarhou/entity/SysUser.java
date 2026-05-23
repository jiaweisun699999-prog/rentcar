package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String phone;
    private String password;
    private Integer role; // 0-普通租客, 1-门店管理员, 2-系统超管
    private Integer creditScore;
    
    private Date createTime;
    private Date updateTime;
    
    @TableLogic
    private Integer isDeleted;
}
