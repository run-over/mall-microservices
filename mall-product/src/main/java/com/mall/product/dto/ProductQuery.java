package com.mall.product.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品分页查询条件。
 *
 * @author 彭桂润
 */
@Data
public class ProductQuery {

    private Long pageNum = 1L;

    private Long pageSize = 10L;

    /** 关键字：匹配商品名称、品牌或副标题 */
    private String keyword;

    private Long categoryId;

    /** 状态过滤；前台只查上架商品 */
    private Integer status;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /** 排序字段：sales-销量，price-价格，createTime-上架时间 */
    private String sortBy;

    /** 是否升序 */
    private Boolean asc = false;
}
