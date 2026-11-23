package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应 mall_order.t_order。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业务订单号，唯一索引 */
    private String orderNo;

    /** 下单用户 */
    private Long userId;

    /** 商品总金额 */
    private BigDecimal totalAmount;

    /** 实付金额（此处未接入优惠，等于总金额） */
    private BigDecimal payAmount;

    /** 订单状态：0 待付款，1 已付款，2 已发货，3 已完成，4 已取消 */
    private Integer status;

    /** 收件人 */
    private String receiver;

    /** 收件电话 */
    private String receiverPhone;

    /** 收件地址 */
    private String receiverAddress;

    /** 买家留言 */
    private String remark;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 取消原因 */
    private String cancelReason;
}
