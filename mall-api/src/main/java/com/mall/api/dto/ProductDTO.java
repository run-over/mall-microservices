package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品信息传输对象，用于订单服务下单时校验商品。
 *
 * @author 彭桂润
 */
@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 商品名称 */
    private String name;

    /** 售价 */
    private BigDecimal price;

    /** 主图地址 */
    private String mainImage;

    /** 状态：1 上架，0 下架 */
    private Integer status;
}
