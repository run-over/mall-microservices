package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品类目数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
