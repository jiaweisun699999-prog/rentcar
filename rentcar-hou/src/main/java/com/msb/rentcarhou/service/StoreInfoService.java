package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.dto.StoreSaveDto;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.vo.StoreInfoVo;

public interface StoreInfoService extends IService<StoreInfo> {
    Page<StoreInfoVo> getStoreList(StoreQueryDto queryDto);

    void addStore(StoreSaveDto saveDto);

    void updateStore(StoreSaveDto saveDto);

    void deleteStore(Long id);
}
