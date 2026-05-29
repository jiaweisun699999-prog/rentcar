package com.msb.rentcarhou.dto;

import lombok.Data;

/**
 * 新增或修改门店时接收前端请求数据的 DTO。
 */
@Data
public class StoreSaveDto {
    /**
     * 门店 ID，新增时不需要，修改时必填。
     */
    private Long id;

    /**
     * 商户名称，后端会根据该名称查找或创建商户。
     */
    private String merchantName;

    /**
     * 门店所在城市。
     */
    private String cityName;

    /**
     * 门店详细地址。
     */
    private String address;

    /**
     * 是否支持送车上门，0 表示不支持，1 表示支持。
     */
    private Integer isSupportDelivery;
}
