package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单状态变更请求，支付与配送服务回调订单服务时使用。
 *
 * @author 彭桂润
 */
@Data
public class OrderStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单号 */
    private String orderNo;

    /** 目标状态，见 MallConstants.ORDER_STATUS_* */
    private Integer status;
}
