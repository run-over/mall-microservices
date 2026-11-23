package com.mall.order.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 下单明细行。
 *
 * @author 彭桂润
 */
@Data
public class OrderItemDTO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为 1")
    private Integer quantity;
}
