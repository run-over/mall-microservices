package com.mall.api.feign;

import com.mall.api.dto.OrderStatusDTO;
import com.mall.api.fallback.OrderFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务调用客户端。
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-order", path = "/orders", fallbackFactory = OrderFeignFallbackFactory.class)
public interface OrderFeignClient {

    /**
     * 更新订单状态（支付回调、发货回调使用）。
     */
    @PutMapping("/status")
    Result<Void> updateStatus(@RequestBody OrderStatusDTO orderStatusDTO);
}
