package com.mall.api.feign;

import com.mall.api.dto.StockChangeDTO;
import com.mall.api.fallback.StockFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务调用客户端。
 * <p>该调用位于 Seata 全局事务中，库存服务作为 RM 参与分支事务，
 * 抛出的业务异常会被全局事务拦截并触发回滚。</p>
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-stock", path = "/stocks", fallbackFactory = StockFeignFallbackFactory.class)
public interface StockFeignClient {

    /**
     * 扣减库存。
     *
     * @param stockChangeDTO 扣减请求
     * @return 扣减结果
     */
    @PostMapping("/deduct")
    Result<Boolean> deduct(@RequestBody StockChangeDTO stockChangeDTO);

    /**
     * 回补库存（订单取消时调用）。
     *
     * @param stockChangeDTO 回补请求
     * @return 回补结果
     */
    @PostMapping("/release")
    Result<Boolean> release(@RequestBody StockChangeDTO stockChangeDTO);
}
