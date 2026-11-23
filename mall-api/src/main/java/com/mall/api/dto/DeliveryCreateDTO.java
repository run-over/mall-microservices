package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建配送单请求，由订单服务在支付成功后发起。
 *
 * @author 彭桂润
 */
@Data
public class DeliveryCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务订单号 */
    private String orderNo;

    /** 收件人 */
    private String receiver;

    /** 收件电话 */
    private String phone;

    /** 收件地址 */
    private String address;

    /** 快递公司 */
    private String company;
}
