package com.mall.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 库存数据访问接口。
 * <p>扣减与回补都使用单条 UPDATE，把"判断 + 修改"合并为一步，
 * 由数据库行锁保证并发安全，避免先查后改的竞态。</p>
 *
 * @author 彭桂润
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 扣减库存：available >= quantity 才更新，影响行数为 0 表示库存不足。
     */
    @Update("UPDATE t_stock SET available = available - #{quantity}, locked = locked + #{quantity}, "
            + "version = version + 1 WHERE product_id = #{productId} AND available >= #{quantity} AND deleted = 0")
    int deduct(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 回补库存：只回补已锁定的部分，防止无中生有地增加可售库存。
     */
    @Update("UPDATE t_stock SET available = available + #{quantity}, locked = locked - #{quantity}, "
            + "version = version + 1 WHERE product_id = #{productId} AND locked >= #{quantity} AND deleted = 0")
    int release(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
