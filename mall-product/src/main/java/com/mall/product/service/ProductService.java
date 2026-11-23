package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.result.PageResult;
import com.mall.product.dto.ProductQuery;
import com.mall.product.dto.ProductSaveDTO;
import com.mall.product.entity.Product;
import com.mall.product.vo.ProductVO;

/**
 * 商品业务接口。
 *
 * @author 彭桂润
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品。
     */
    PageResult<ProductVO> pageProducts(ProductQuery query);

    /**
     * 查询商品详情。
     */
    ProductVO getDetail(Long productId);

    /**
     * 新增商品（管理员）。
     */
    Long createProduct(ProductSaveDTO saveDTO);

    /**
     * 修改商品（管理员）。
     */
    void updateProduct(Long productId, ProductSaveDTO saveDTO);

    /**
     * 删除商品（管理员，逻辑删除）。
     */
    void removeProduct(Long productId);

    /**
     * 上架 / 下架商品（管理员）。
     */
    void changeStatus(Long productId, Integer status);

    /**
     * 累加销量，支付成功后由订单服务调用。
     */
    void increaseSales(Long productId, Integer quantity);
}
