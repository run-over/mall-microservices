package com.mall.product.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品新增 / 修改请求。
 *
 * @author 彭桂润
 */
@Data
public class ProductSaveDTO {

    @NotNull(message = "类目不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;

    private String brand;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String mainImage;

    private String detail;

    private String unit;

    /** 上架状态：1 上架，0 下架 */
    private Integer status;
}
