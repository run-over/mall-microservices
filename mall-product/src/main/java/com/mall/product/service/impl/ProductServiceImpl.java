package com.mall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.ResultCode;
import com.mall.product.dto.ProductQuery;
import com.mall.product.dto.ProductSaveDTO;
import com.mall.product.entity.Category;
import com.mall.product.entity.Product;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.service.ProductService;
import com.mall.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品业务实现。
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ProductVO> pageProducts(ProductQuery query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getBrand, keyword)
                    .or().like(Product::getSubtitle, keyword));
        }
        wrapper.eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId());
        wrapper.eq(query.getStatus() != null, Product::getStatus, query.getStatus());
        wrapper.ge(query.getMinPrice() != null, Product::getPrice, query.getMinPrice());
        wrapper.le(query.getMaxPrice() != null, Product::getPrice, query.getMaxPrice());

        // 排序字段白名单，禁止把前端传入的字段直接拼进 SQL
        String sortBy = StringUtils.hasText(query.getSortBy()) ? query.getSortBy() : "createTime";
        boolean asc = Boolean.TRUE.equals(query.getAsc());
        switch (sortBy) {
            case "sales":
                wrapper.orderBy(true, asc, Product::getSales);
                break;
            case "price":
                wrapper.orderBy(true, asc, Product::getPrice);
                break;
            default:
                wrapper.orderByDesc(Product::getCreateTime);
                break;
        }

        Page<Product> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<Product> records = page.getRecords();
        Map<Long, String> categoryNames = loadCategoryNames(records);
        return PageResult.of(page, product -> {
            ProductVO vo = BeanUtil.copyProperties(product, ProductVO.class);
            vo.setCategoryName(categoryNames.get(product.getCategoryId()));
            return vo;
        });
    }

    @Override
    public ProductVO getDetail(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BizException(ResultCode.PRODUCT_NOT_FOUND);
        }
        ProductVO vo = BeanUtil.copyProperties(product, ProductVO.class);
        Category category = categoryMapper.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductSaveDTO saveDTO) {
        checkAdmin();
        Category category = categoryMapper.selectById(saveDTO.getCategoryId());
        BizException.throwIf(category == null, "商品类目不存在");

        Product product = new Product();
        BeanUtil.copyProperties(saveDTO, product);
        product.setSales(0);
        product.setStatus(saveDTO.getStatus() == null ? 1 : saveDTO.getStatus());
        this.save(product);
        log.info("商品已创建: productId={}, name={}", product.getId(), product.getName());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long productId, ProductSaveDTO saveDTO) {
        checkAdmin();
        Product product = this.getById(productId);
        BizException.throwIf(product == null, ResultCode.PRODUCT_NOT_FOUND);
        BeanUtil.copyProperties(saveDTO, product);
        product.setId(productId);
        this.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProduct(Long productId) {
        checkAdmin();
        BizException.throwIf(this.getById(productId) == null, ResultCode.PRODUCT_NOT_FOUND);
        this.removeById(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long productId, Integer status) {
        checkAdmin();
        Product product = this.getById(productId);
        BizException.throwIf(product == null, ResultCode.PRODUCT_NOT_FOUND);
        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    public void increaseSales(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return;
        }
        int rows = this.baseMapper.increaseSales(productId, quantity);
        if (rows == 0) {
            log.warn("累加销量未命中商品: productId={}", productId);
        }
    }

    /**
     * 批量查类目名称，避免在分页结果里逐条查询造成 N+1。
     */
    private Map<Long, String> loadCategoryNames(List<Product> records) {
        if (CollectionUtils.isEmpty(records)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> categoryIds = records.stream()
                .map(Product::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可维护商品信息");
        }
    }
}
