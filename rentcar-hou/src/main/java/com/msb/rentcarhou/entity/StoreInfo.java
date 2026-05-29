package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 门店实体类，对应数据库表 store_info。
 */
@Data
@TableName("store_info")
public class StoreInfo {
    /**
     * 门店主键 ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属商户 ID，对应 merchant_info 表的 id。
     */
    private Long merchantId;

    /**
     * 门店所在城市名称，用于城市筛选。
     */
    private String cityName;

    /**
     * 门店详细服务地址。
     */
    private String address;

    /**
     * 是否支持送车上门，0 表示不支持，1 表示支持。
     */
    private Integer isSupportDelivery;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;

    /**
     * 逻辑删除标记，由 MyBatis-Plus 自动处理。
     */
    @TableLogic
    private Integer isDeleted;
}
