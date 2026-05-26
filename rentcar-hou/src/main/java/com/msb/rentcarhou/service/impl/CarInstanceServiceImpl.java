package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.CarInstanceReqDto;
import com.msb.rentcarhou.entity.CarInstance;
import com.msb.rentcarhou.mapper.CarInstanceMapper;
import com.msb.rentcarhou.mapper.CarModelMapper;
import com.msb.rentcarhou.service.CarInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CarInstanceServiceImpl extends ServiceImpl<CarInstanceMapper, CarInstance> implements CarInstanceService {
    private final CarModelMapper carModelMapper;

    @Override
    public void addInstance(CarInstanceReqDto reqDto) {
        if (reqDto == null) {
            throw new RuntimeException("车辆参数不能为空");
        }
        if (reqDto.getModelId() == null) {
            throw new RuntimeException("车型ID不能为空");
        }
        if (carModelMapper.selectById(reqDto.getModelId()) == null) {
            throw new RuntimeException("车型不存在");
        }
        if (reqDto.getStoreId() == null) {
            throw new RuntimeException("门店ID不能为空");
        }
        if (!StringUtils.hasText(reqDto.getPlateNumber())) {
            throw new RuntimeException("车牌号不能为空");
        }
        if (reqDto.getDailyRentPrice() == null || reqDto.getDailyRentPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("日租金必须大于0");
        }

        CarInstance carInstance = new CarInstance();
        carInstance.setModelId(reqDto.getModelId());
        carInstance.setStoreId(reqDto.getStoreId());
        carInstance.setPlateNumber(reqDto.getPlateNumber());
        carInstance.setDailyRentPrice(reqDto.getDailyRentPrice());
        carInstance.setStatus(0);
        this.save(carInstance);
    }

    @Override
    public Page<CarInstance> getInstanceList(Long modelId, int page, int pageSize) {
        LambdaQueryWrapper<CarInstance> wrapper = new LambdaQueryWrapper<>();
        if (modelId != null) {
            wrapper.eq(CarInstance::getModelId, modelId);
        }
        wrapper.orderByDesc(CarInstance::getCreateTime);
        return this.page(new Page<>(page, pageSize), wrapper);
    }
}
