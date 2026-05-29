package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.dto.StoreSaveDto;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.vo.StoreInfoVo;

/**
 * 门店业务接口，定义门店模块对外提供的核心业务能力。
 */
public interface StoreInfoService extends IService<StoreInfo> {
    /**
     * 分页查询门店列表。
     *
     * @param queryDto 查询条件
     * @return 门店分页展示数据
     */
    Page<StoreInfoVo> getStoreList(StoreQueryDto queryDto);

    /**
     * 新增门店。
     *
     * @param saveDto 门店保存参数
     */
    void addStore(StoreSaveDto saveDto);

    /**
     * 修改门店。
     *
     * @param saveDto 门店保存参数，必须包含门店 ID
     */
    void updateStore(StoreSaveDto saveDto);

    /**
     * 删除门店。
     *
     * @param id 门店 ID
     */
    void deleteStore(Long id);
}
