package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("car_order")
public class CarOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long carId;
    private Long pickupStoreId;
    private Long dropoffStoreId;
    private Date startTime;
    private Date endTime;
    private BigDecimal totalAmount;
    private BigDecimal rentFee;
    private BigDecimal basicInsuranceFee;
    private BigDecimal handlingFee;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
