package com.mall.api.feign;

import com.mall.api.dto.ProductDTO;
import com.mall.api.fallback.ProductFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务调用客户端。
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-product", path = "/products", fallbackFactory = ProductFeignFallbackFactory.class)
public interface ProductFeignClient {

    /**
     * 查询商品详情（下单时校验价格与状态）。
     */
    @GetMapping("/{productId}")
    Result<ProductDTO> getById(@PathVariable("productId") Long productId);

    /**
     * 累加商品销量（支付成功后调用）。
     */
    @PostMapping("/{productId}/sales")
    Result<Void> increaseSales(@PathVariable("productId") Long productId,
                               @RequestParam("quantity") Integer quantity);
}
