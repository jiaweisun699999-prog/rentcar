package com.msb.rentcarhou.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类 (Entity)
 * 作用：与 MySQL 数据库中的 `car_order` 表进行字段的一一对应（映射）。
 * MyBatis-Plus 靠它来自动生成 SQL。
 */
@Data
@TableName("car_order") // 告诉框架，这个类对应数据库的 "car_order" 表
public class CarOrder {
    /** 主键 ID，配置为自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 业务使用的订单号 (对外展示用，防止暴露真实的数据库记录行数) */
    private String orderNo;
    /** 下单用户的 ID */
    private Long userId;
    /** 具体分配给用户使用的车辆实例 ID (关联 car_instance 表) */
    private Long carId;
    /** 取车门店 ID */
    private Long pickupStoreId;
    /** 还车门店 ID */
    private Long dropoffStoreId;
    /** 实际记录的取车时间 */
    private Date startTime;
    /** 实际记录的还车时间 */
    private Date endTime;
    /** 总计金额 (用 BigDecimal 防止金额计算产生精度丢失问题) */
    private BigDecimal totalAmount;
    /** 车辆租金小计 */
    private BigDecimal rentFee;
    /** 基础保险费小计 */
    private BigDecimal basicInsuranceFee;
    /** 手续费小计 */
    private BigDecimal handlingFee;
    /**
     * 核心生命周期状态码:
     * 0=待支付, 1=已支付/待取车, 2=租赁中, 3=待还车结算, 4=已完成, 5=已取消
     */
    private Integer status;
    /** 记录创建时间 */
    private Date createTime;
    /** 记录最后修改时间 */
    private Date updateTime;

    /** 
     * 逻辑删除标识 (MyBatis-Plus 提供)。
     * 删除数据时不会真正抹除记录，而是把这个字段设为 1，方便后期查账。 
     */
    @TableLogic
    private Integer isDeleted;
}
