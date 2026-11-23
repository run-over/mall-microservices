package com.mall.api.feign;

import com.mall.api.dto.PaymentCreateDTO;
import com.mall.api.fallback.PaymentFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 支付服务调用客户端。
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-payment", path = "/payments", fallbackFactory = PaymentFeignFallbackFactory.class)
public interface PaymentFeignClient {

    /**
     * 创建支付单，返回支付单号。
     */
    @PostMapping
    Result<String> createPayment(@RequestBody PaymentCreateDTO paymentCreateDTO);
}
