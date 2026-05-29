package com.msb.rentcarhou.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 门店列表返回给前端的展示对象。
 */
@Data
public class StoreInfoVo {
    /**
     * 门店 ID。
     */
    private Long id;

    /**
     * 所属商户 ID。
     */
    private Long merchantId;

    /**
     * 所属商户名称。
     */
    private String merchantName;

    /**
     * 门店所在城市。
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
     * 创建时间，按北京时间格式化返回给前端。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
