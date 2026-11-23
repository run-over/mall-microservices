package com.mall.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体，对应 mall_payment.t_payment。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment")
public class Payment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 支付单号，唯一索引 */
    private String payNo;

    /** 业务订单号 */
    private String orderNo;

    /** 付款用户 */
    private Long userId;

    /** 应付金额 */
    private BigDecimal amount;

    /** 支付渠道：ALIPAY / WECHAT / BALANCE */
    private String channel;

    /** 状态：0 待支付，1 支付成功，2 已关闭 */
    private Integer status;

    /** 第三方交易号 */
    private String tradeNo;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    /** 回调原文，便于对账与排障 */
    private String notifyContent;
}
