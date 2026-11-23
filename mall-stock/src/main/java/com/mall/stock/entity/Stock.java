package com.mall.stock.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存实体，对应 mall_stock.t_stock。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock")
public class Stock extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 商品 ID，与商品服务的主键逻辑关联（不建物理外键） */
    private Long productId;

    /** 冗余商品名称，便于库存流水与对账 */
    private String productName;

    /** 总库存 */
    private Integer total;

    /** 可售库存 */
    private Integer available;

    /** 锁定库存（已下单未支付/未发货） */
    private Integer locked;

    /** 乐观锁版本号 */
    @Version
    private Integer version;
}
