package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.entity.MerchantInfo;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.mapper.MerchantInfoMapper;
import com.msb.rentcarhou.mapper.StoreInfoMapper;
import com.msb.rentcarhou.service.StoreService;
import com.msb.rentcarhou.vo.StoreVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl extends ServiceImpl<StoreInfoMapper, StoreInfo> implements StoreService {

    private final MerchantInfoMapper merchantInfoMapper;

    @Override
    public Page<StoreVo> getStoreList(StoreQueryDto queryDto) {
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        LambdaQueryWrapper<StoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getCityName())) {
            queryWrapper.eq(StoreInfo::getCityName, queryDto.getCityName().trim());
        }
        queryWrapper.orderByDesc(StoreInfo::getCreateTime);

        Page<StoreInfo> storePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        Page<StoreVo> resultPage = new Page<>(storePage.getCurrent(), storePage.getSize(), storePage.getTotal());
        resultPage.setRecords(buildStoreVos(storePage.getRecords()));
        return resultPage;
    }

    private List<StoreVo> buildStoreVos(List<StoreInfo> stores) {
        if (stores == null || stores.isEmpty()) {
            return List.of();
        }
        List<Long> merchantIds = stores.stream().map(StoreInfo::getMerchantId).distinct().toList();
        Map<Long, MerchantInfo> merchantMap = merchantInfoMapper.selectList(new LambdaQueryWrapper<MerchantInfo>().in(MerchantInfo::getId, merchantIds))
                .stream().collect(Collectors.toMap(MerchantInfo::getId, merchant -> merchant));
        return stores.stream().map(store -> buildStoreVo(store, merchantMap.get(store.getMerchantId()))).toList();
    }

    private StoreVo buildStoreVo(StoreInfo storeInfo, MerchantInfo merchantInfo) {
        StoreVo vo = new StoreVo();
        vo.setId(storeInfo.getId());
        vo.setMerchantId(storeInfo.getMerchantId());
        vo.setCityName(storeInfo.getCityName());
        vo.setAddress(storeInfo.getAddress());
        vo.setIsSupportDelivery(storeInfo.getIsSupportDelivery());
        vo.setCreateTime(storeInfo.getCreateTime());
        if (merchantInfo != null) {
            vo.setMerchantName(merchantInfo.getMerchantName());
            vo.setServiceScore(merchantInfo.getServiceScore());
        }
        return vo;
    }
}
