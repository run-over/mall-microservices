package com.mall.stock.controller;

import com.mall.api.dto.StockChangeDTO;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.stock.dto.StockSaveDTO;
import com.mall.stock.entity.Stock;
import com.mall.stock.entity.StockLog;
import com.mall.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 库存接口。
 *
 * @author 彭桂润
 */
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 分页查询库存。
     */
    @GetMapping
    public Result<PageResult<Stock>> page(@RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                          @RequestParam(value = "productId", required = false) Long productId,
                                          @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(stockService.pageStocks(pageNum, pageSize, productId, keyword));
    }

    /**
     * 按商品查询库存。
     */
    @GetMapping("/product/{productId}")
    public Result<Stock> getByProductId(@PathVariable("productId") Long productId) {
        return Result.success(stockService.getByProductId(productId));
    }

    /**
     * 初始化 / 调整库存（管理员）。
     */
    @PostMapping
    public Result<Void> init(@Valid @RequestBody StockSaveDTO saveDTO) {
        stockService.initStock(saveDTO);
        return Result.success();
    }

    /**
     * 扣减库存（Seata 分支事务入口，由订单服务 Feign 调用）。
     */
    @PostMapping("/deduct")
    public Result<Boolean> deduct(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return Result.success(stockService.deduct(stockChangeDTO));
    }

    /**
     * 回补库存（订单取消）。
     */
    @PostMapping("/release")
    public Result<Boolean> release(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return Result.success(stockService.release(stockChangeDTO));
    }

    /**
     * 查询某订单的库存流水。
     */
    @GetMapping("/logs/{orderNo}")
    public Result<List<StockLog>> logs(@PathVariable("orderNo") String orderNo) {
        return Result.success(stockService.listLogsByOrderNo(orderNo));
    }
}
