package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.service.StoreService;
import com.msb.rentcarhou.vo.StoreVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@CrossOrigin
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    public Result<Page<StoreVo>> list(StoreQueryDto queryDto) {
        try {
            return Result.success(storeService.getStoreList(queryDto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
