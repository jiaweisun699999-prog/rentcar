package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("merchant_info")
public class MerchantInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String merchantName;
    private BigDecimal serviceScore;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
