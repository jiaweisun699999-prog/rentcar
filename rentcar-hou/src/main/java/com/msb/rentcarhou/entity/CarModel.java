package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("car_model")
public class CarModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String brandSeries;
    private String carType;
    private String seatsDoors;
    private String mainImage;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
