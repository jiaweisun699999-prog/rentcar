package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("store_info")
public class StoreInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String cityName;
    private String address;
    private Integer isSupportDelivery;
    private Date createTime;
    private Date updateTime;

    @TableLogic
    private Integer isDeleted;
}
