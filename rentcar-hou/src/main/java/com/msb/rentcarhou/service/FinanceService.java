package com.msb.rentcarhou.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.rentcarhou.dto.FinanceQueryDto;
import com.msb.rentcarhou.entity.PaymentRecord;
import com.msb.rentcarhou.vo.FinanceRecordVo;

public interface FinanceService extends IService<PaymentRecord> {
    Page<FinanceRecordVo> getFinanceList(FinanceQueryDto queryDto);
}
