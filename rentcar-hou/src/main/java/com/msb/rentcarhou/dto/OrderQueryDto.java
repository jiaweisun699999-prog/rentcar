package com.msb.rentcarhou.dto;

import lombok.Data;

/**
 * 订单查询数据传输对象
 * 作用：前端调用“我的订单列表”接口时，用来接收分页和过滤条件的参数
 */
@Data
public class OrderQueryDto {
    /** 请求的页码，默认为第一页 */
    private Integer page = 1;
    /** 每页展示的数据条数，默认为一页10条 */
    private Integer pageSize = 10;
    /** 订单状态过滤条件 (比如只想看"待支付"的订单，就传0；不传代表查所有状态) */
    private Integer status;
}
