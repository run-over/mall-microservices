package com.mall.order.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建订单请求。
 *
 * @author 彭桂润
 */
@Data
public class OrderCreateDTO {

    /** 收货人，通常从用户服务的地址簿中带出 */
    @NotBlank(message = "收件人不能为空")
    private String receiver;

    @NotBlank(message = "收件电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收件地址不能为空")
    private String receiverAddress;

    /** 买家留言 */
    private String remark;

    /** 商品明细，至少一行 */
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDTO> items;
}
