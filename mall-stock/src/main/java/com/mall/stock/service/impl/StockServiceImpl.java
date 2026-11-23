package com.mall.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.StockChangeDTO;
import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.ResultCode;
import com.mall.stock.dto.StockSaveDTO;
import com.mall.stock.entity.Stock;
import com.mall.stock.entity.StockLog;
import com.mall.stock.mapper.StockLogMapper;
import com.mall.stock.mapper.StockMapper;
import com.mall.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 库存业务实现。
 * <p>扣减采用"单条条件 UPDATE + 流水幂等"双重保护：
 * UPDATE 的 available >= quantity 条件保证不会超卖，
 * 流水唯一索引保证同一订单重复请求只扣一次。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final StockLogMapper stockLogMapper;

    @Override
    public PageResult<Stock> pageStocks(Long pageNum, Long pageSize, Long productId, String keyword) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, Stock::getProductId, productId);
        wrapper.like(StringUtils.hasText(keyword), Stock::getProductName, keyword);
        wrapper.orderByDesc(Stock::getUpdateTime);
        Page<Stock> page = this.page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Stock getByProductId(Long productId) {
        Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getProductId, productId), false);
        if (stock == null) {
            throw new BizException(ResultCode.STOCK_NOT_FOUND);
        }
        return stock;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initStock(StockSaveDTO saveDTO) {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可调整库存");
        }
        Stock exists = this.getOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, saveDTO.getProductId()), false);
        if (exists == null) {
            Stock stock = new Stock();
            stock.setProductId(saveDTO.getProductId());
            stock.setProductName(saveDTO.getProductName());
            stock.setTotal(saveDTO.getTotal());
            stock.setAvailable(saveDTO.getTotal());
            stock.setLocked(0);
            stock.setVersion(0);
            this.save(stock);
            log.info("库存初始化: productId={}, total={}", saveDTO.getProductId(), saveDTO.getTotal());
            return;
        }
        // 调整总库存时，已锁定的部分不能被覆盖掉
        int diff = saveDTO.getTotal() - exists.getTotal();
        exists.setTotal(saveDTO.getTotal());
        exists.setAvailable(Math.max(0, exists.getAvailable() + diff));
        if (StringUtils.hasText(saveDTO.getProductName())) {
            exists.setProductName(saveDTO.getProductName());
        }
        this.updateById(exists);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(StockChangeDTO stockChangeDTO) {
        Long productId = stockChangeDTO.getProductId();
        Integer quantity = stockChangeDTO.getQuantity();

        // 幂等：同一订单同一商品只允许扣减一次
        if (StringUtils.hasText(stockChangeDTO.getOrderNo()) && logExists(stockChangeDTO, MallConstants.STOCK_LOG_DEDUCT)) {
            log.info("库存扣减请求重复，直接返回成功: orderNo={}, productId={}",
                    stockChangeDTO.getOrderNo(), productId);
            return true;
        }

        int rows = this.baseMapper.deduct(productId, quantity);
        if (rows == 0) {
            // 抛异常让 Seata 全局事务回滚，订单不会留下"有单无货"的脏数据
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH, "商品库存不足，商品 ID: " + productId);
        }
        Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getProductId, productId), false);
        saveLog(stockChangeDTO, MallConstants.STOCK_LOG_DEDUCT, stock == null ? null : stock.getAvailable());
        log.info("库存扣减成功: orderNo={}, productId={}, quantity={}, 剩余={}",
                stockChangeDTO.getOrderNo(), productId, quantity, stock == null ? "-" : stock.getAvailable());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean release(StockChangeDTO stockChangeDTO) {
        Long productId = stockChangeDTO.getProductId();
        Integer quantity = stockChangeDTO.getQuantity();

        if (StringUtils.hasText(stockChangeDTO.getOrderNo()) && logExists(stockChangeDTO, MallConstants.STOCK_LOG_RELEASE)) {
            log.info("库存回补请求重复，直接返回成功: orderNo={}", stockChangeDTO.getOrderNo());
            return true;
        }
        int rows = this.baseMapper.release(productId, quantity);
        if (rows == 0) {
            throw new BizException(ResultCode.STOCK_NOT_FOUND, "该商品没有可回补的锁定库存");
        }
        Stock stock = this.getOne(new LambdaQueryWrapper<Stock>().eq(Stock::getProductId, productId), false);
        saveLog(stockChangeDTO, MallConstants.STOCK_LOG_RELEASE, stock == null ? null : stock.getAvailable());
        return true;
    }

    @Override
    public List<StockLog> listLogsByOrderNo(String orderNo) {
        return stockLogMapper.selectList(new LambdaQueryWrapper<StockLog>()
                .eq(StockLog::getOrderNo, orderNo)
                .orderByDesc(StockLog::getCreateTime));
    }

    private boolean logExists(StockChangeDTO dto, int type) {
        return stockLogMapper.selectCount(new LambdaQueryWrapper<StockLog>()
                .eq(StockLog::getOrderNo, dto.getOrderNo())
                .eq(StockLog::getProductId, dto.getProductId())
                .eq(StockLog::getType, type)) > 0;
    }

    private void saveLog(StockChangeDTO dto, int type, Integer afterAvailable) {
        StockLog stockLog = new StockLog();
        stockLog.setProductId(dto.getProductId());
        stockLog.setOrderNo(dto.getOrderNo());
        stockLog.setQuantity(dto.getQuantity());
        stockLog.setType(type);
        stockLog.setAfterAvailable(afterAvailable);
        stockLogMapper.insert(stockLog);
    }
}
