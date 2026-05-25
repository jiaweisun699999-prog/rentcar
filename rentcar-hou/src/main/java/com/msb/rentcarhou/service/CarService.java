package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.CarModelAddDto;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.entity.CarModel;
import com.msb.rentcarhou.vo.CarModelListVo;
import com.msb.rentcarhou.vo.CarModelVo;

import java.util.List;

public interface CarService extends IService<CarModel> {
    Page<CarModelListVo> getModelList(CarModelQueryDto queryDto);
    List<CarModelVo> getRecommendModels();
    void addModel(CarModelAddDto addDto);
    void deleteModel(Long id);
}
