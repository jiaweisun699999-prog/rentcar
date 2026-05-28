package com.msb.rentcarhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.rentcarhou.entity.CarOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单模块的数据访问层 (Mapper / DAO)
 * 作用：专门负责与 MySQL 数据库进行交互。
 * 继承 MyBatis-Plus 的 BaseMapper<CarOrder> 后，
 * 框架会自动为我们生成针对 car_order 表的单表 CRUD（增删改查）SQL 语句，无需手写繁琐的 XML。
 * 
 * 在整个闭环中，它是 Service 层的“底层打工人”，负责执行真正的 SQL 并把结果封装成 Entity 实体类。
 */
@Mapper // 标识这是一个 MyBatis 的 Mapper 接口，Spring 会自动为其创建代理实现类并放进容器中
public interface CarOrderMapper extends BaseMapper<CarOrder> {
    // 此处可以定义 MyBatis-Plus 默认方法无法满足的复杂自定义 SQL 查询接口
}
