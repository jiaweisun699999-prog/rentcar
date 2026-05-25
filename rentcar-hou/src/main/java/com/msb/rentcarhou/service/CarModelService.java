package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.dto.CarModelReqDto;
import com.msb.rentcarhou.entity.CarModel;
import com.msb.rentcarhou.vo.CarModelVo;

import java.util.List;

public interface CarModelService extends IService<CarModel> {
    Page<CarModelVo> getModelList(CarModelQueryDto queryDto);
    List<CarModelVo> getRecommendList();
    void addModel(CarModelReqDto reqDto);
    void deleteModel(Long id);
}
