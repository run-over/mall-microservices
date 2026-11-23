package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体，对应 mall_product.t_product。
 * <p>库存不放在本表，由库存服务单独维护，避免商品与库存两个关注点耦合在同一个库。</p>
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属类目 */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 卖点副标题 */
    private String subtitle;

    /** 品牌 */
    private String brand;

    /** 售价 */
    private BigDecimal price;

    /** 划线价 */
    private BigDecimal originalPrice;

    /** 主图地址 */
    private String mainImage;

    /** 详情描述 */
    private String detail;

    /** 累计销量 */
    private Integer sales;

    /** 计量单位 */
    private String unit;

    /** 状态：1 上架，0 下架 */
    private Integer status;
}
