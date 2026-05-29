package com.msb.rentcarhou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.rentcarhou.entity.StoreInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店表 Mapper，继承 MyBatis-Plus 的 BaseMapper 后拥有基础 CRUD 能力。
 */
@Mapper
public interface StoreInfoMapper extends BaseMapper<StoreInfo> {
}
