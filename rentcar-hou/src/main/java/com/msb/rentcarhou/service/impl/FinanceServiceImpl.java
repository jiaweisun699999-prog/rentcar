package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.FinanceQueryDto;
import com.msb.rentcarhou.entity.PaymentRecord;
import com.msb.rentcarhou.mapper.PaymentRecordMapper;
import com.msb.rentcarhou.service.FinanceService;
import com.msb.rentcarhou.vo.FinanceRecordVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

@Service
public class FinanceServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements FinanceService {

    @Override
    public Page<FinanceRecordVo> getFinanceList(FinanceQueryDto queryDto) {
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getStartDate())) {
            queryWrapper.ge(PaymentRecord::getCreateTime, parseStartDate(queryDto.getStartDate()));
        }
        if (queryDto != null && StringUtils.hasText(queryDto.getEndDate())) {
            queryWrapper.le(PaymentRecord::getCreateTime, parseEndDate(queryDto.getEndDate()));
        }
        queryWrapper.orderByDesc(PaymentRecord::getCreateTime);

        Page<PaymentRecord> paymentPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<FinanceRecordVo> records = paymentPage.getRecords().stream().map(this::convertToVo).toList();

        Page<FinanceRecordVo> resultPage = new Page<>(paymentPage.getCurrent(), paymentPage.getSize(), paymentPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    private FinanceRecordVo convertToVo(PaymentRecord paymentRecord) {
        FinanceRecordVo vo = new FinanceRecordVo();
        vo.setTransactionId(paymentRecord.getTradeNo());
        vo.setOrderId(paymentRecord.getOrderNo());
        vo.setAmount(paymentRecord.getAmount());
        vo.setTradeType(paymentRecord.getTradeType());
        vo.setTradeTypeName(getTradeTypeName(paymentRecord.getTradeType()));
        vo.setType(getFinanceType(paymentRecord.getTradeType()));
        vo.setStatus(paymentRecord.getStatus());
        vo.setRemark(getRemark(paymentRecord));
        vo.setCreateTime(paymentRecord.getCreateTime());
        return vo;
    }

    private Integer getFinanceType(Integer tradeType) {
        if (tradeType != null && tradeType == 4) {
            return 3;
        }
        return 1;
    }

    private String getTradeTypeName(Integer tradeType) {
        if (tradeType == null) {
            return "未知类型";
        }
        return switch (tradeType) {
            case 1 -> "租金支付";
            case 2 -> "车辆押金冻结";
            case 3 -> "违章押金冻结";
            case 4 -> "押金退还";
            default -> "未知类型";
        };
    }

    private String getRemark(PaymentRecord paymentRecord) {
        String tradeTypeName = getTradeTypeName(paymentRecord.getTradeType());
        String statusName = getStatusName(paymentRecord.getStatus());
        return tradeTypeName + "-" + statusName;
    }

    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "处理中";
            case 1 -> "成功";
            case 2 -> "失败";
            default -> "未知";
        };
    }

    private Date parseStartDate(String date) {
        return parseDate(date, true);
    }

    private Date parseEndDate(String date) {
        return parseDate(date, false);
    }

    private Date parseDate(String date, boolean start) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime dateTime = start ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式应为yyyy-MM-dd");
        }
    }
}
