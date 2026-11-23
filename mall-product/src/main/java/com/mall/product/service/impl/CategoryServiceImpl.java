package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.ResultCode;
import com.mall.product.entity.Category;
import com.mall.product.entity.Product;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品类目业务实现。
 *
 * @author 彭桂润
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public List<Category> listAll(Integer status) {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(status != null, Category::getStatus, status)
                .orderByAsc(Category::getParentId)
                .orderByAsc(Category::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(Category category) {
        checkAdmin();
        category.setId(null);
        category.setParentId(category.getParentId() == null ? 0L : category.getParentId());
        category.setSort(category.getSort() == null ? 0 : category.getSort());
        category.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        this.save(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long categoryId, Category category) {
        checkAdmin();
        BizException.throwIf(this.getById(categoryId) == null, "类目不存在");
        category.setId(categoryId);
        this.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCategory(Long categoryId) {
        checkAdmin();
        long children = this.count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId));
        BizException.throwIf(children > 0, "该类目下存在子类目，无法删除");
        long products = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, categoryId));
        BizException.throwIf(products > 0, "该类目下仍有商品，无法删除");
        this.removeById(categoryId);
    }

    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可维护类目");
        }
    }
}
