package com.mall.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mall.order.entity.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图对象。
 *
 * @author 彭桂润
 */
@Data
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    /** 状态中文描述，便于前端直接展示 */
    private String statusDesc;

    private String receiver;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    private List<OrderItem> items;
}
