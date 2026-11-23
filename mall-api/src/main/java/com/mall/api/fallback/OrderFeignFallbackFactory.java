package com.mall.api.fallback;

import com.mall.api.dto.OrderStatusDTO;
import com.mall.api.feign.OrderFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 订单服务降级工厂。
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class OrderFeignFallbackFactory implements FallbackFactory<OrderFeignClient> {

    @Override
    public OrderFeignClient create(Throwable cause) {
        log.error("调用订单服务失败，已触发降级: {}", cause.getMessage());
        return orderStatusDTO -> Result.fail(ResultCode.REMOTE_CALL_ERROR, "订单服务暂时不可用");
    }
}
