package com.mall.api.feign;

import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.api.fallback.DeliveryFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 配送服务调用客户端。
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-delivery", path = "/deliveries", fallbackFactory = DeliveryFeignFallbackFactory.class)
public interface DeliveryFeignClient {

    /**
     * 创建配送单（发货），返回配送单号。
     */
    @PostMapping
    Result<String> createDelivery(@RequestBody DeliveryCreateDTO deliveryCreateDTO);
}
