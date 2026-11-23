package com.mall.api.fallback;

import com.mall.api.dto.StockChangeDTO;
import com.mall.api.feign.StockFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 库存服务降级工厂。
 * <p>库存扣减是下单的强依赖，不允许"降级成功后继续下单"，
 * 因此降级结果必须是失败，让订单服务的全局事务回滚。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class StockFeignFallbackFactory implements FallbackFactory<StockFeignClient> {

    @Override
    public StockFeignClient create(Throwable cause) {
        log.error("调用库存服务失败，已触发降级: {}", cause.getMessage());
        return new StockFeignClient() {
            @Override
            public Result<Boolean> deduct(StockChangeDTO stockChangeDTO) {
                return Result.fail(ResultCode.REMOTE_CALL_ERROR, "库存服务暂时不可用，下单失败");
            }

            @Override
            public Result<Boolean> release(StockChangeDTO stockChangeDTO) {
                return Result.fail(ResultCode.REMOTE_CALL_ERROR, "库存服务暂时不可用，回补失败");
            }
        };
    }
}
