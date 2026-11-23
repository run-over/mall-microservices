package com.mall.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存流水实体，对应 mall_stock.t_stock_log。
 * <p>流水表上的 (order_no, product_id, type) 唯一索引是扣减幂等的最后一道防线。</p>
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_log")
public class StockLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long productId;

    /** 关联订单号 */
    private String orderNo;

    /** 变更数量 */
    private Integer quantity;

    /** 类型：1 扣减，2 回补 */
    private Integer type;

    /** 变更后可用库存，便于追溯 */
    private Integer afterAvailable;
}
