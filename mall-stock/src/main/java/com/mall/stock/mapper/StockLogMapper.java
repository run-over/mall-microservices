package com.mall.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.stock.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}
