package com.mall.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.api.dto.StockChangeDTO;
import com.mall.common.result.PageResult;
import com.mall.stock.dto.StockSaveDTO;
import com.mall.stock.entity.Stock;
import com.mall.stock.entity.StockLog;

import java.util.List;

/**
 * 库存业务接口。
 *
 * @author 彭桂润
 */
public interface StockService extends IService<Stock> {

    /**
     * 分页查询库存。
     */
    PageResult<Stock> pageStocks(Long pageNum, Long pageSize, Long productId, String keyword);

    /**
     * 按商品查询库存。
     */
    Stock getByProductId(Long productId);

    /**
     * 初始化或调整库存（管理员）。
     */
    void initStock(StockSaveDTO saveDTO);

    /**
     * 扣减库存，参与 Seata 全局事务。
     *
     * @param stockChangeDTO 扣减请求
     * @return 是否成功
     */
    boolean deduct(StockChangeDTO stockChangeDTO);

    /**
     * 回补库存（订单取消）。
     */
    boolean release(StockChangeDTO stockChangeDTO);

    /**
     * 查询某订单的库存流水。
     */
    List<StockLog> listLogsByOrderNo(String orderNo);
}
