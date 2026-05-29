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
            // 接收前端分页和日期筛选参数，交给 Service 层完成具体查询和 VO 转换
            Page<FinanceRecordVo> page = financeService.getFinanceList(queryDto);
            // 使用统一 Result 格式返回，前端 axios 响应拦截器会自动解包 data
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
