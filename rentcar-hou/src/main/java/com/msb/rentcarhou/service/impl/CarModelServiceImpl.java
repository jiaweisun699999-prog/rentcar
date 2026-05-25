package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.dto.CarModelReqDto;
import com.msb.rentcarhou.entity.CarInstance;
import com.msb.rentcarhou.entity.CarModel;
import com.msb.rentcarhou.mapper.CarModelMapper;
import com.msb.rentcarhou.service.CarInstanceService;
import com.msb.rentcarhou.service.CarModelService;
import com.msb.rentcarhou.vo.CarModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarModelServiceImpl extends ServiceImpl<CarModelMapper, CarModel> implements CarModelService {
    private final CarInstanceService carInstanceService;

    @Override
    public Page<CarModelVo> getModelList(CarModelQueryDto queryDto) {
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        LambdaQueryWrapper<CarModel> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getKeyword())) {
            queryWrapper.like(CarModel::getBrandSeries, queryDto.getKeyword())
                    .or()
                    .like(CarModel::getCarType, queryDto.getKeyword());
        }
        queryWrapper.orderByDesc(CarModel::getCreateTime);

        Page<CarModel> modelPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        Map<Long, BigDecimal> priceMap = getMinPriceMap(modelPage.getRecords().stream().map(CarModel::getId).toList());
        List<CarModelVo> records = modelPage.getRecords().stream()
                .map(model -> convertToVo(model, priceMap.get(model.getId())))
                .toList();

        Page<CarModelVo> resultPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<CarModelVo> getRecommendList() {
        LambdaQueryWrapper<CarModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(CarModel::getCreateTime).last("limit 8");
        List<CarModel> models = this.list(queryWrapper);
        Map<Long, BigDecimal> priceMap = getMinPriceMap(models.stream().map(CarModel::getId).toList());
        return models.stream().map(model -> convertToVo(model, priceMap.get(model.getId()))).toList();
    }

    @Override
    public void addModel(CarModelReqDto reqDto) {
        if (reqDto == null) {
            throw new RuntimeException("车型参数不能为空");
        }
        if (!StringUtils.hasText(reqDto.getBrandSeries())) {
            throw new RuntimeException("品牌车系不能为空");
        }
        if (!StringUtils.hasText(reqDto.getCarType())) {
            throw new RuntimeException("车辆类型不能为空");
        }
        if (!StringUtils.hasText(reqDto.getSeatsDoors())) {
            throw new RuntimeException("配置不能为空");
        }

        CarModel carModel = new CarModel();
        carModel.setBrandSeries(reqDto.getBrandSeries());
        carModel.setCarType(reqDto.getCarType());
        carModel.setSeatsDoors(reqDto.getSeatsDoors());
        carModel.setMainImage(reqDto.getMainImage());
        this.save(carModel);
    }

    @Override
    public void deleteModel(Long id) {
        if (id == null) {
            throw new RuntimeException("车型ID不能为空");
        }
        this.removeById(id);
    }

    private Map<Long, BigDecimal> getMinPriceMap(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<CarInstance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CarInstance::getModelId, modelIds);
        return carInstanceService.list(queryWrapper).stream()
                .filter(instance -> instance.getModelId() != null && instance.getDailyRentPrice() != null)
                .collect(Collectors.groupingBy(
                        CarInstance::getModelId,
                        Collectors.collectingAndThen(
                                Collectors.mapping(CarInstance::getDailyRentPrice, Collectors.minBy(BigDecimal::compareTo)),
                                optional -> optional.orElse(null)
                        )
                ));
    }

    private CarModelVo convertToVo(CarModel carModel, BigDecimal dailyPrice) {
        CarModelVo vo = new CarModelVo();
        vo.setId(carModel.getId());
        vo.setBrandSeries(carModel.getBrandSeries());
        vo.setCarType(carModel.getCarType());
        vo.setSeatsDoors(carModel.getSeatsDoors());
        vo.setMainImage(carModel.getMainImage());
        vo.setDailyPrice(Objects.requireNonNullElse(dailyPrice, BigDecimal.ZERO));
        vo.setStatus(1);
        vo.setCreateTime(carModel.getCreateTime());
        return vo;
    }
}
