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
        // 1. 分页参数兜底，避免前端未传或传入非法页码导致查询异常
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        // 2. 动态构造查询条件：有开始日期就查开始之后，有结束日期就查结束之前
        LambdaQueryWrapper<PaymentRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDto != null && StringUtils.hasText(queryDto.getStartDate())) {
            queryWrapper.ge(PaymentRecord::getCreateTime, parseStartDate(queryDto.getStartDate()));
        }
        if (queryDto != null && StringUtils.hasText(queryDto.getEndDate())) {
            queryWrapper.le(PaymentRecord::getCreateTime, parseEndDate(queryDto.getEndDate()));
        }
        // 3. 财务流水按创建时间倒序展示，保证最新交易排在最前面
        queryWrapper.orderByDesc(PaymentRecord::getCreateTime);

        // 4. 使用 MyBatis-Plus 分页查询 payment_record 表，得到数据库实体分页结果
        Page<PaymentRecord> paymentPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 5. 将数据库实体转换为前端展示需要的 VO，隐藏无关字段并补充中文业务含义
        List<FinanceRecordVo> records = paymentPage.getRecords().stream().map(this::convertToVo).toList();

        // 6. 重新组装 Page<FinanceRecordVo>，保留 current、size、total 等分页信息
        Page<FinanceRecordVo> resultPage = new Page<>(paymentPage.getCurrent(), paymentPage.getSize(), paymentPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    private FinanceRecordVo convertToVo(PaymentRecord paymentRecord) {
        // 把数据库字段转换成前端表格更容易理解的字段名和展示内容
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
        // 押金退还归为退款赔付，其余交易在页面上归为营业收入
        if (tradeType != null && tradeType == 4) {
            return 3;
        }
        return 1;
    }

    private String getTradeTypeName(Integer tradeType) {
        // 将数据库中的交易类型数字翻译成前端可直接展示的中文说明
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
        // 备注由交易类型和交易状态组合生成，例如：租金支付-成功
        String tradeTypeName = getTradeTypeName(paymentRecord.getTradeType());
        String statusName = getStatusName(paymentRecord.getStatus());
        return tradeTypeName + "-" + statusName;
    }

    private String getStatusName(Integer status) {
        // 将交易状态码转换成中文状态说明
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
            // 开始日期取当天 00:00:00，结束日期取当天最后一刻，避免漏查结束日期当天流水
            LocalDateTime dateTime = start ? localDate.atStartOfDay() : localDate.atTime(LocalTime.MAX);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式应为yyyy-MM-dd");
        }
    }
}
