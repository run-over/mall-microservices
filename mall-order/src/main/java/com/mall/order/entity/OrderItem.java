package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细实体，对应 mall_order.t_order_item。
 * <p>下单时把商品名称与价格快照写入明细，避免商品后续改价影响历史订单。</p>
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_item")
public class OrderItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属订单 ID */
    private Long orderId;

    /** 订单号，冗余字段便于按订单号查询 */
    private String orderNo;

    private Long productId;

    /** 下单时的商品名称快照 */
    private String productName;

    /** 下单时的商品图片快照 */
    private String productImage;

    /** 下单时的单价快照 */
    private BigDecimal price;

    private Integer quantity;

    /** 小计 = 单价 × 数量 */
    private BigDecimal totalPrice;
}
