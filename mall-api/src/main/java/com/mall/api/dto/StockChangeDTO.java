package com.mall.api.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 库存变更请求，下单扣减与取消回补共用。
 *
 * @author 彭桂润
 */
@Data
public class StockChangeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    /** 变更数量，必须大于 0 */
    @NotNull(message = "变更数量不能为空")
    @Min(value = 1, message = "变更数量必须大于 0")
    private Integer quantity;

    /** 关联订单号，用于幂等与流水追溯 */
    private String orderNo;
}
