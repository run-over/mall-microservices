package com.mall.delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.common.result.PageResult;
import com.mall.delivery.entity.Delivery;

/**
 * 配送业务接口。
 *
 * @author 彭桂润
 */
public interface DeliveryService extends IService<Delivery> {

    /**
     * 创建配送单（发货），同一订单只允许一张配送单。
     *
     * @param createDTO 发货请求
     * @return 配送单号
     */
    String createDelivery(DeliveryCreateDTO createDTO);

    /**
     * 按订单号查询物流信息。
     */
    Delivery getByOrderNo(String orderNo);

    /**
     * 更新配送状态。
     */
    void updateStatus(String deliveryNo, Integer status);

    /**
     * 分页查询配送单。
     */
    PageResult<Delivery> pageDeliveries(Long pageNum, Long pageSize, Integer status);
}
