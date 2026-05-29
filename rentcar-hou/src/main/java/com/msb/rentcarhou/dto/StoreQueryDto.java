package com.msb.rentcarhou.dto;

import lombok.Data;

/**
 * 门店列表查询参数。
 */
@Data
public class StoreQueryDto {
    /**
     * 当前页码，默认第 1 页。
     */
    private Integer page = 1;

    /**
     * 每页记录数，默认每页 10 条。
     */
    private Integer pageSize = 10;

    /**
     * 城市名称筛选条件，支持按城市模糊查询。
     */
    private String cityName;
}
