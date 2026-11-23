package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.api.dto.OrderStatusDTO;
import com.mall.common.result.PageResult;
import com.mall.order.dto.OrderCreateDTO;
import com.mall.order.entity.Order;
import com.mall.order.vo.OrderVO;

/**
 * 订单业务接口。
 *
 * @author 彭桂润
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单：写入订单与明细，并通过 Seata 全局事务扣减库存。
     *
     * @param createDTO 下单请求
     * @return 订单号
     */
    String createOrder(OrderCreateDTO createDTO);

    /**
     * 分页查询订单。
     */
    PageResult<OrderVO> pageOrders(Long pageNum, Long pageSize, Integer status, String keyword);

    /**
     * 查询订单详情（含明细）。
     */
    OrderVO getDetail(String orderNo);

    /**
     * 取消订单并回补库存。
     */
    void cancelOrder(String orderNo, String reason);

    /**
     * 更新订单状态，供支付、配送服务回调。
     */
    void updateStatus(OrderStatusDTO statusDTO);

    /**
     * 发货：调用配送服务生成配送单并把订单置为已发货。
     */
    void shipOrder(String orderNo, String company);

    /**
     * 确认收货。
     */
    void confirmReceipt(String orderNo);
}
