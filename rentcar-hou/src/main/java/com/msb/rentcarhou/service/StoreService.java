package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.vo.StoreVo;

public interface StoreService extends IService<StoreInfo> {
    Page<StoreVo> getStoreList(StoreQueryDto queryDto);
}
