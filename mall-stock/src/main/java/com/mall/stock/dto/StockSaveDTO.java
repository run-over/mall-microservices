package com.mall.stock.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 库存初始化 / 调整请求。
 *
 * @author 彭桂润
 */
@Data
public class StockSaveDTO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    private String productName;

    /** 总库存 */
    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负数")
    private Integer total;
}
