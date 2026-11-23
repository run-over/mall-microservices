package com.mall.product.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import com.mall.product.dto.ProductQuery;
import com.mall.product.dto.ProductSaveDTO;
import com.mall.product.service.ProductService;
import com.mall.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 商品接口。
 * <p>商品详情接口标注了 @SentinelResource 并配置 blockHandler，
 * 用于演示热点参数限流：同一商品被并发刷详情时只限流该参数值，不影响其他商品。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品（可匿名访问，前台只查上架商品）。
     */
    @GetMapping
    public Result<PageResult<ProductVO>> page(@ModelAttribute ProductQuery query) {
        return Result.success(productService.pageProducts(query));
    }

    /**
     * 商品详情：热点参数限流的保护点。
     */
    @GetMapping("/{productId}")
    @SentinelResource(value = "productDetail", blockHandler = "detailBlocked")
    public Result<ProductVO> detail(@PathVariable("productId") Long productId) {
        return Result.success(productService.getDetail(productId));
    }

    /**
     * 热点参数被限流后的兜底方法，方法签名需与原方法一致并追加 BlockException。
     */
    public Result<ProductVO> detailBlocked(Long productId, BlockException ex) {
        log.warn("商品详情触发热点参数限流: productId={}, rule={}", productId, ex.getRule());
        return Result.fail(ResultCode.ERROR, "当前商品访问人数过多，请稍后再试");
    }

    /**
     * 新增商品（管理员）。
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductSaveDTO saveDTO) {
        return Result.success("商品已创建", productService.createProduct(saveDTO));
    }

    /**
     * 修改商品（管理员）。
     */
    @PutMapping("/{productId}")
    public Result<Void> update(@PathVariable("productId") Long productId,
                               @Valid @RequestBody ProductSaveDTO saveDTO) {
        productService.updateProduct(productId, saveDTO);
        return Result.success();
    }

    /**
     * 上架 / 下架（管理员）。
     */
    @PutMapping("/{productId}/status")
    public Result<Void> changeStatus(@PathVariable("productId") Long productId,
                                     @RequestParam("status") Integer status) {
        productService.changeStatus(productId, status);
        return Result.success();
    }

    /**
     * 删除商品（管理员）。
     */
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable("productId") Long productId) {
        productService.removeProduct(productId);
        return Result.success();
    }

    /**
     * 累加销量，供订单服务在支付成功后调用。
     */
    @PostMapping("/{productId}/sales")
    public Result<Void> increaseSales(@PathVariable("productId") Long productId,
                                      @RequestParam("quantity") Integer quantity) {
        productService.increaseSales(productId, quantity);
        return Result.success();
    }
}
