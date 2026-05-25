package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.dto.StoreSaveDto;
import com.msb.rentcarhou.entity.MerchantInfo;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.mapper.MerchantInfoMapper;
import com.msb.rentcarhou.mapper.StoreInfoMapper;
import com.msb.rentcarhou.service.StoreInfoService;
import com.msb.rentcarhou.vo.StoreInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreInfoServiceImpl extends ServiceImpl<StoreInfoMapper, StoreInfo> implements StoreInfoService {

    private final MerchantInfoMapper merchantInfoMapper;

    @Override
    public Page<StoreInfoVo> getStoreList(StoreQueryDto queryDto) {
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        LambdaQueryWrapper<StoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getCityName())) {
            queryWrapper.like(StoreInfo::getCityName, queryDto.getCityName().trim());
        }
        queryWrapper.orderByDesc(StoreInfo::getCreateTime);

        Page<StoreInfo> storePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        Map<Long, MerchantInfo> merchantMap = getMerchantMap(storePage.getRecords());
        List<StoreInfoVo> records = storePage.getRecords().stream()
                .map(storeInfo -> convertToVo(storeInfo, merchantMap.get(storeInfo.getMerchantId())))
                .toList();

        Page<StoreInfoVo> resultPage = new Page<>(storePage.getCurrent(), storePage.getSize(), storePage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStore(StoreSaveDto saveDto) {
        checkSaveParams(saveDto, false);
        MerchantInfo merchantInfo = getOrCreateMerchant(saveDto.getMerchantName().trim());

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setMerchantId(merchantInfo.getId());
        storeInfo.setCityName(saveDto.getCityName().trim());
        storeInfo.setAddress(saveDto.getAddress().trim());
        storeInfo.setIsSupportDelivery(normalizeSupportDelivery(saveDto.getIsSupportDelivery()));
        this.save(storeInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStore(StoreSaveDto saveDto) {
        checkSaveParams(saveDto, true);
        StoreInfo storeInfo = this.getById(saveDto.getId());
        if (storeInfo == null) {
            throw new RuntimeException("门店不存在");
        }

        MerchantInfo merchantInfo = getOrCreateMerchant(saveDto.getMerchantName().trim());
        storeInfo.setMerchantId(merchantInfo.getId());
        storeInfo.setCityName(saveDto.getCityName().trim());
        storeInfo.setAddress(saveDto.getAddress().trim());
        storeInfo.setIsSupportDelivery(normalizeSupportDelivery(saveDto.getIsSupportDelivery()));
        this.updateById(storeInfo);
    }

    @Override
    public void deleteStore(Long id) {
        if (id == null) {
            throw new RuntimeException("门店ID不能为空");
        }
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("门店不存在或已删除");
        }
    }

    private Map<Long, MerchantInfo> getMerchantMap(List<StoreInfo> stores) {
        List<Long> merchantIds = stores.stream()
                .map(StoreInfo::getMerchantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (merchantIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return merchantInfoMapper.selectBatchIds(merchantIds).stream()
                .collect(Collectors.toMap(MerchantInfo::getId, Function.identity(), (oldValue, newValue) -> oldValue));
    }

    private StoreInfoVo convertToVo(StoreInfo storeInfo, MerchantInfo merchantInfo) {
        StoreInfoVo vo = new StoreInfoVo();
        vo.setId(storeInfo.getId());
        vo.setMerchantId(storeInfo.getMerchantId());
        vo.setMerchantName(merchantInfo == null ? null : merchantInfo.getMerchantName());
        vo.setCityName(storeInfo.getCityName());
        vo.setAddress(storeInfo.getAddress());
        vo.setIsSupportDelivery(storeInfo.getIsSupportDelivery());
        vo.setCreateTime(storeInfo.getCreateTime());
        return vo;
    }

    private MerchantInfo getOrCreateMerchant(String merchantName) {
        MerchantInfo merchantInfo = merchantInfoMapper.selectOne(new LambdaQueryWrapper<MerchantInfo>()
                .eq(MerchantInfo::getMerchantName, merchantName)
                .last("limit 1"));
        if (merchantInfo != null) {
            return merchantInfo;
        }

        MerchantInfo newMerchantInfo = new MerchantInfo();
        newMerchantInfo.setMerchantName(merchantName);
        newMerchantInfo.setServiceScore(new BigDecimal("5.0"));
        merchantInfoMapper.insert(newMerchantInfo);
        return newMerchantInfo;
    }

    private void checkSaveParams(StoreSaveDto saveDto, boolean requireId) {
        if (saveDto == null) {
            throw new RuntimeException("门店参数不能为空");
        }
        if (requireId && saveDto.getId() == null) {
            throw new RuntimeException("门店ID不能为空");
        }
        if (!StringUtils.hasText(saveDto.getMerchantName())) {
            throw new RuntimeException("商户名称不能为空");
        }
        if (!StringUtils.hasText(saveDto.getCityName())) {
            throw new RuntimeException("城市名称不能为空");
        }
        if (!StringUtils.hasText(saveDto.getAddress())) {
            throw new RuntimeException("门店地址不能为空");
        }
        Integer isSupportDelivery = saveDto.getIsSupportDelivery();
        if (isSupportDelivery != null && isSupportDelivery != 0 && isSupportDelivery != 1) {
            throw new RuntimeException("是否支持送车上门只能为0或1");
        }
    }

    private Integer normalizeSupportDelivery(Integer isSupportDelivery) {
        return isSupportDelivery == null ? 0 : isSupportDelivery;
    }
}
