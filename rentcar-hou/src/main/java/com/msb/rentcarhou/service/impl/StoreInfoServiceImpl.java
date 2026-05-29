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

    /**
     * 商户 Mapper，用于根据商户名称或商户 ID 查询商户信息。
     */
    private final MerchantInfoMapper merchantInfoMapper;

    /**
     * 分页查询门店列表，并补充每个门店所属商户的名称。
     *
     * @param queryDto 查询参数，包含页码、每页条数和城市名称
     * @return 门店分页展示数据
     */
    @Override
    public Page<StoreInfoVo> getStoreList(StoreQueryDto queryDto) {
        // 兜底处理分页参数，避免前端不传或传入非法页码导致分页异常。
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10
                : queryDto.getPageSize();

        LambdaQueryWrapper<StoreInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getCityName())) {
            // 城市名称使用模糊匹配，前端输入“北京”也能匹配“北京市”。
            queryWrapper.like(StoreInfo::getCityName, queryDto.getCityName().trim());
        }
        queryWrapper.orderByDesc(StoreInfo::getCreateTime);

        // 先分页查询门店表，再批量查询商户表，避免循环查询商户造成 N+1 查询问题。
        Page<StoreInfo> storePage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        Map<Long, MerchantInfo> merchantMap = getMerchantMap(storePage.getRecords());
        List<StoreInfoVo> records = storePage.getRecords().stream()
                .map(storeInfo -> convertToVo(storeInfo, merchantMap.get(storeInfo.getMerchantId())))
                .toList();

        // 数据库查询结果是 StoreInfo，需要重新封装成前端展示用的 StoreInfoVo 分页对象。
        Page<StoreInfoVo> resultPage = new Page<>(storePage.getCurrent(), storePage.getSize(), storePage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 新增门店。
     *
     * @param saveDto 新增门店参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStore(StoreSaveDto saveDto) {
        // 新增时不要求传门店 ID。
        checkSaveParams(saveDto, false);
        // 前端传的是商户名称，后端需要转换成商户 ID 后再保存门店。
        MerchantInfo merchantInfo = getOrCreateMerchant(saveDto.getMerchantName().trim());

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setMerchantId(merchantInfo.getId());
        storeInfo.setCityName(saveDto.getCityName().trim());
        storeInfo.setAddress(saveDto.getAddress().trim());
        storeInfo.setIsSupportDelivery(normalizeSupportDelivery(saveDto.getIsSupportDelivery()));
        this.save(storeInfo);
    }

    /**
     * 修改门店。
     *
     * @param saveDto 修改门店参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStore(StoreSaveDto saveDto) {
        // 修改时必须传门店 ID。
        checkSaveParams(saveDto, true);
        StoreInfo storeInfo = this.getById(saveDto.getId());
        if (storeInfo == null) {
            throw new RuntimeException("门店不存在");
        }

        // 支持修改门店所属商户；如果商户不存在，则自动创建后再关联。
        MerchantInfo merchantInfo = getOrCreateMerchant(saveDto.getMerchantName().trim());
        storeInfo.setMerchantId(merchantInfo.getId());
        storeInfo.setCityName(saveDto.getCityName().trim());
        storeInfo.setAddress(saveDto.getAddress().trim());
        storeInfo.setIsSupportDelivery(normalizeSupportDelivery(saveDto.getIsSupportDelivery()));
        this.updateById(storeInfo);
    }

    /**
     * 删除门店。
     *
     * @param id 门店 ID
     */
    @Override
    public void deleteStore(Long id) {
        if (id == null) {
            throw new RuntimeException("门店ID不能为空");
        }
        // StoreInfo 中配置了 @TableLogic，因此 removeById 执行的是逻辑删除。
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new RuntimeException("门店不存在");
        }
    }

    /**
     * 根据门店列表中的商户 ID 批量查询商户信息。
     *
     * @param stores 当前页门店列表
     * @return key 为商户 ID，value 为商户信息的 Map
     */
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

    /**
     * 将门店实体和商户实体转换成前端展示对象。
     *
     * @param storeInfo    门店实体
     * @param merchantInfo 商户实体，可能为空
     * @return 门店展示对象
     */
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

    /**
     * 根据商户名称查询商户；如果不存在，则创建一个默认评分为 5.0 的新商户。
     *
     * @param merchantName 商户名称
     * @return 已存在或新创建的商户信息
     */
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

    /**
     * 校验新增或修改门店时的参数。
     *
     * @param saveDto   门店保存参数
     * @param requireId 是否必须校验门店 ID，新增为 false，修改为 true
     */
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

    /**
     * 规范化是否支持送车上门字段。
     *
     * @param isSupportDelivery 前端传入的支持状态
     * @return 如果前端未传则默认返回 0，否则返回原值
     */
    private Integer normalizeSupportDelivery(Integer isSupportDelivery) {
        return isSupportDelivery == null ? 0 : isSupportDelivery;
    }

}
