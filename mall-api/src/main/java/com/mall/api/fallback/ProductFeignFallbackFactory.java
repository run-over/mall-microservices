package com.mall.api.fallback;

import com.mall.api.dto.ProductDTO;
import com.mall.api.feign.ProductFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 商品服务降级工厂。
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeignClient> {

    @Override
    public ProductFeignClient create(Throwable cause) {
        log.error("调用商品服务失败，已触发降级: {}", cause.getMessage());
        return new ProductFeignClient() {
            @Override
            public Result<ProductDTO> getById(Long productId) {
                return Result.fail(ResultCode.REMOTE_CALL_ERROR, "商品服务暂时不可用");
            }

            @Override
            public Result<Void> increaseSales(Long productId, Integer quantity) {
                // 销量属于非核心统计，降级时直接忽略，不阻塞支付主流程
                log.warn("商品销量累加降级，productId={}, quantity={}", productId, quantity);
                return Result.success();
            }
        };
    }
}
