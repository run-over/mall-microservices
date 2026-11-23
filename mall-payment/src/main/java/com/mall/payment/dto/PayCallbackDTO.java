package com.mall.payment.dto;

import lombok.Data;

/**
 * 支付回调请求（模拟第三方支付平台异步通知）。
 *
 * @author 彭桂润
 */
@Data
public class PayCallbackDTO {

    /** 第三方交易号 */
    private String tradeNo;

    /** 是否支付成功 */
    private Boolean success;
}
