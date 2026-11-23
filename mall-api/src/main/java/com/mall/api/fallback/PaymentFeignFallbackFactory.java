package com.mall.api.fallback;

import com.mall.api.dto.PaymentCreateDTO;
import com.mall.api.feign.PaymentFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 支付服务降级工厂。
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class PaymentFeignFallbackFactory implements FallbackFactory<PaymentFeignClient> {

    @Override
    public PaymentFeignClient create(Throwable cause) {
        log.error("调用支付服务失败，已触发降级: {}", cause.getMessage());
        return paymentCreateDTO -> Result.fail(ResultCode.REMOTE_CALL_ERROR, "支付服务暂时不可用");
    }
}
