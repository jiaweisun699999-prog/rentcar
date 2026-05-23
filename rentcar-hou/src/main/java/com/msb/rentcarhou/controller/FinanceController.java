package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.FinanceQueryDto;
import com.msb.rentcarhou.service.FinanceService;
import com.msb.rentcarhou.vo.FinanceRecordVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@CrossOrigin
public class FinanceController {
    private final FinanceService financeService;

    @GetMapping("/list")
    public Result<Page<FinanceRecordVo>> list(FinanceQueryDto queryDto) {
        try {
            Page<FinanceRecordVo> page = financeService.getFinanceList(queryDto);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
