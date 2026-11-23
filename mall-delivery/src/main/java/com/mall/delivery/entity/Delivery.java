package com.mall.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 配送单实体，对应 mall_delivery.t_delivery。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_delivery")
public class Delivery extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 配送单号，唯一索引 */
    private String deliveryNo;

    /** 业务订单号，唯一索引（一单一配送） */
    private String orderNo;

    /** 快递公司 */
    private String company;

    /** 快递单号 */
    private String trackingNo;

    /** 收件人 */
    private String receiver;

    /** 收件电话 */
    private String phone;

    /** 收件地址 */
    private String address;

    /** 状态：0 待揽收，1 运输中，2 已签收 */
    private Integer status;

    /** 签收时间 */
    private LocalDateTime signTime;
}
