package com.mall.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.OrderStatusDTO;
import com.mall.api.dto.PaymentCreateDTO;
import com.mall.api.feign.OrderFeignClient;
import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import com.mall.payment.dto.PayCallbackDTO;
import com.mall.payment.entity.Payment;
import com.mall.payment.mapper.PaymentMapper;
import com.mall.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付业务实现。
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private static final DateTimeFormatter PAY_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderFeignClient orderFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPayment(PaymentCreateDTO createDTO) {
        BizException.throwIf(!StringUtils.hasText(createDTO.getOrderNo()), "订单号不能为空");
        BizException.throwIf(createDTO.getAmount() == null || createDTO.getAmount().signum() <= 0, "支付金额必须大于 0");

        // 幂等：同一订单已有待支付单时直接复用，避免重复支付
        Payment waiting = this.getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, createDTO.getOrderNo())
                .eq(Payment::getStatus, MallConstants.PAY_STATUS_WAITING), false);
        if (waiting != null) {
            log.info("订单已存在待支付单，直接复用: orderNo={}, payNo={}",
                    createDTO.getOrderNo(), waiting.getPayNo());
            return waiting.getPayNo();
        }

        Payment payment = new Payment();
        payment.setPayNo(generatePayNo());
        payment.setOrderNo(createDTO.getOrderNo());
        payment.setUserId(createDTO.getUserId() != null ? createDTO.getUserId() : UserContext.requireUserId());
        payment.setAmount(createDTO.getAmount());
        payment.setChannel(StringUtils.hasText(createDTO.getChannel()) ? createDTO.getChannel() : "ALIPAY");
        payment.setStatus(MallConstants.PAY_STATUS_WAITING);
        this.save(payment);
        log.info("支付单已创建: payNo={}, orderNo={}, amount={}",
                payment.getPayNo(), payment.getOrderNo(), payment.getAmount());
        return payment.getPayNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callback(String payNo, PayCallbackDTO callbackDTO) {
        Payment payment = getByPayNo(payNo);
        // 重复回调直接返回，保证幂等
        if (MallConstants.PAY_STATUS_SUCCESS == payment.getStatus()) {
            log.info("支付单已处理，忽略重复回调: payNo={}", payNo);
            return;
        }
        BizException.throwIf(!Boolean.TRUE.equals(callbackDTO.getSuccess()), "支付失败，请重新发起支付");

        payment.setStatus(MallConstants.PAY_STATUS_SUCCESS);
        payment.setTradeNo(StringUtils.hasText(callbackDTO.getTradeNo())
                ? callbackDTO.getTradeNo() : "TRADE" + System.currentTimeMillis());
        payment.setPayTime(LocalDateTime.now());
        payment.setNotifyContent("模拟回调: success=true, tradeNo=" + payment.getTradeNo());
        this.updateById(payment);

        // 通知订单服务：支付成功。失败时抛出异常让支付单回滚，等待平台重推
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        orderStatusDTO.setOrderNo(payment.getOrderNo());
        orderStatusDTO.setStatus(MallConstants.ORDER_STATUS_PAID);
        Result<Void> result = orderFeignClient.updateStatus(orderStatusDTO);
        if (!result.isSuccess()) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR, "通知订单服务失败：" + result.getMessage());
        }
        log.info("支付成功并已通知订单服务: payNo={}, orderNo={}", payNo, payment.getOrderNo());
    }

    @Override
    public Payment getByPayNo(String payNo) {
        Payment payment = this.getOne(new LambdaQueryWrapper<Payment>().eq(Payment::getPayNo, payNo), false);
        if (payment == null) {
            throw new BizException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    @Override
    public Payment getByOrderNo(String orderNo) {
        return this.getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1"), false);
    }

    @Override
    public PageResult<Payment> pagePayments(Long pageNum, Long pageSize, Integer status) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isAdmin()) {
            wrapper.eq(Payment::getUserId, UserContext.requireUserId());
        }
        wrapper.eq(status != null, Payment::getStatus, status);
        wrapper.orderByDesc(Payment::getCreateTime);
        Page<Payment> page = this.page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), wrapper);
        return PageResult.of(page);
    }

    private String generatePayNo() {
        return "PAY" + LocalDateTime.now().format(PAY_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
