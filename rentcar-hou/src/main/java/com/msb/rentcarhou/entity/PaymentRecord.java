package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String tradeNo;
    private Integer tradeType;
    private BigDecimal amount;
    private BigDecimal creditWaivedAmount;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
