package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建支付单请求。
 *
 * @author 彭桂润
 */
@Data
public class PaymentCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务订单号 */
    private String orderNo;

    /** 支付用户 */
    private Long userId;

    /** 应付金额 */
    private BigDecimal amount;

    /** 支付渠道：ALIPAY / WECHAT / BALANCE */
    private String channel;
}
