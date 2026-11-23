package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.product.entity.Category;

import java.util.List;

/**
 * 商品类目业务接口。
 *
 * @author 彭桂润
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询类目树（平铺返回，前端按 parentId 组装）。
     */
    List<Category> listAll(Integer status);

    /**
     * 新增类目（管理员）。
     */
    Long createCategory(Category category);

    /**
     * 修改类目（管理员）。
     */
    void updateCategory(Long categoryId, Category category);

    /**
     * 删除类目（管理员），存在子类目或商品时拒绝删除。
     */
    void removeCategory(Long categoryId);
}
