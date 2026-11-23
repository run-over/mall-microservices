package com.mall.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.api.dto.PaymentCreateDTO;
import com.mall.common.result.PageResult;
import com.mall.payment.dto.PayCallbackDTO;
import com.mall.payment.entity.Payment;

/**
 * 支付业务接口。
 *
 * @author 彭桂润
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付单，同一订单重复发起时复用未支付的支付单。
     *
     * @param createDTO 支付请求
     * @return 支付单号
     */
    String createPayment(PaymentCreateDTO createDTO);

    /**
     * 支付结果回调：更新支付单状态并通知订单服务。
     */
    void callback(String payNo, PayCallbackDTO callbackDTO);

    /**
     * 查询支付单。
     */
    Payment getByPayNo(String payNo);

    /**
     * 按订单号查询支付单。
     */
    Payment getByOrderNo(String orderNo);

    /**
     * 分页查询支付单。
     */
    PageResult<Payment> pagePayments(Long pageNum, Long pageSize, Integer status);
}
