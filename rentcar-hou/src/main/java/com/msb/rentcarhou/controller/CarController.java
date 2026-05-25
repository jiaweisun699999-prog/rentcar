package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.CarInstanceReqDto;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.dto.CarModelReqDto;
import com.msb.rentcarhou.service.CarInstanceService;
import com.msb.rentcarhou.service.CarModelService;
import com.msb.rentcarhou.vo.CarModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
@CrossOrigin
public class CarController {
    private final CarModelService carModelService;
    private final CarInstanceService carInstanceService;

    @GetMapping("/recommend")
    public Result<List<CarModelVo>> recommend() {
        return Result.success(carModelService.getRecommendList());
    }

    @GetMapping("/model/list")
    public Result<Page<CarModelVo>> modelList(CarModelQueryDto queryDto) {
        return Result.success(carModelService.getModelList(queryDto));
    }

    @PostMapping("/model/add")
    public Result<Void> addModel(@RequestBody CarModelReqDto reqDto) {
        carModelService.addModel(reqDto);
        return Result.success("车型上架成功", null);
    }

    @DeleteMapping("/model/delete/{id}")
    public Result<Void> deleteModel(@PathVariable Long id) {
        carModelService.deleteModel(id);
        return Result.success("车型下架成功", null);
    }

    @PostMapping("/instance/add")
    public Result<Void> addInstance(@RequestBody CarInstanceReqDto reqDto) {
        carInstanceService.addInstance(reqDto);
        return Result.success("车辆录入成功", null);
    }
}
