package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子累加销量，避免先查后改造成的并发覆盖。
     *
     * @param productId 商品 ID
     * @param quantity  增量
     * @return 影响行数
     */
    @Update("UPDATE t_product SET sales = sales + #{quantity} WHERE id = #{productId} AND deleted = 0")
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
