package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("car_instance")
public class CarInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long modelId;
    private Long storeId;
    private String plateNumber;
    private BigDecimal dailyRentPrice;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
