package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.CarInstanceReqDto;
import com.msb.rentcarhou.entity.CarInstance;

public interface CarInstanceService extends IService<CarInstance> {
    void addInstance(CarInstanceReqDto reqDto);
    Page<CarInstance> getInstanceList(Long modelId, int page, int pageSize);
}
