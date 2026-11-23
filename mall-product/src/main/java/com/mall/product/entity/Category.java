package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品类目实体，对应 mall_product.t_category。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 类目名称 */
    private String name;

    /** 父类目 ID，0 表示一级类目 */
    private Long parentId;

    /** 同级排序值，越小越靠前 */
    private Integer sort;

    /** 状态：1 启用，0 停用 */
    private Integer status;
}
