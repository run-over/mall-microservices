package com.mall.api.fallback;

import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.api.feign.DeliveryFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 配送服务降级工厂。
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class DeliveryFeignFallbackFactory implements FallbackFactory<DeliveryFeignClient> {

    @Override
    public DeliveryFeignClient create(Throwable cause) {
        log.error("调用配送服务失败，已触发降级: {}", cause.getMessage());
        return deliveryCreateDTO -> Result.fail(ResultCode.REMOTE_CALL_ERROR, "配送服务暂时不可用");
    }
}
