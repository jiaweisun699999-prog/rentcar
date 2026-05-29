package com.msb.rentcarhou.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 下单预览视图对象
 * 作用：用户选好时间和地点，还没点支付前，用来展示“费用明细账单”给用户看的
 */
@Data
public class OrderPreviewVo {
    /** 根据前后时间自动算出来的租车天数 (不足1天按1天算) */
    private Integer rentDays;
    /** 车辆租金总计 */
    private BigDecimal rentFee;
    /** 保险费总计 */
    private BigDecimal basicInsuranceFee;
    /** 手续费 (一般是固定金额) */
    private BigDecimal handlingFee;
    /** 所有费用加起来的总金额 */
    private BigDecimal totalAmount;
    /** 车辆押金 (如果支持芝麻信用免押，这里通常返回 0) */
    private BigDecimal depositAmount;
}
