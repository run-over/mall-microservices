package com.mall.payment.controller;

import com.mall.api.dto.PaymentCreateDTO;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.payment.dto.PayCallbackDTO;
import com.mall.payment.entity.Payment;
import com.mall.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口。
 *
 * @author 彭桂润
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付单。
     */
    @PostMapping
    public Result<String> create(@RequestBody PaymentCreateDTO createDTO) {
        return Result.success("支付单已创建", paymentService.createPayment(createDTO));
    }

    /**
     * 支付回调（模拟第三方平台的异步通知）。
     */
    @PostMapping("/{payNo}/callback")
    public Result<Void> callback(@PathVariable("payNo") String payNo,
                                 @RequestBody PayCallbackDTO callbackDTO) {
        paymentService.callback(payNo, callbackDTO);
        return Result.success("支付成功", null);
    }

    /**
     * 按支付单号查询。
     */
    @GetMapping("/{payNo}")
    public Result<Payment> detail(@PathVariable("payNo") String payNo) {
        return Result.success(paymentService.getByPayNo(payNo));
    }

    /**
     * 按订单号查询支付单。
     */
    @GetMapping("/order/{orderNo}")
    public Result<Payment> byOrderNo(@PathVariable("orderNo") String orderNo) {
        return Result.success(paymentService.getByOrderNo(orderNo));
    }

    /**
     * 分页查询支付单。
     */
    @GetMapping("/page")
    public Result<PageResult<Payment>> page(@RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(paymentService.pagePayments(pageNum, pageSize, status));
    }
}
