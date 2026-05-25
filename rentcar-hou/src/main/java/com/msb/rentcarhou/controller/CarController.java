package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.CarModelAddDto;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.service.CarService;
import com.msb.rentcarhou.vo.CarModelListVo;
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

    private final CarService carService;

    @GetMapping("/model/list")
    public Result<Page<CarModelListVo>> getModelList(CarModelQueryDto queryDto) {
        try {
            Page<CarModelListVo> page = carService.getModelList(queryDto);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public Result<List<CarModelVo>> recommend() {
        try {
            List<CarModelVo> list = carService.getRecommendModels();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/model/add")
    public Result<Void> addModel(@RequestBody CarModelAddDto addDto) {
        try {
            carService.addModel(addDto);
            return Result.success("车型上架成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/model/delete/{id}")
    public Result<Void> deleteModel(@PathVariable Long id) {
        try {
            carService.deleteModel(id);
            return Result.success("车型下架成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
