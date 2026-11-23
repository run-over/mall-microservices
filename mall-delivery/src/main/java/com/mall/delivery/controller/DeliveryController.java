package com.mall.delivery.controller;

import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.delivery.entity.Delivery;
import com.mall.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配送接口。
 *
 * @author 彭桂润
 */
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 创建配送单（发货），由订单服务 Feign 调用。
     */
    @PostMapping
    public Result<String> create(@RequestBody DeliveryCreateDTO createDTO) {
        return Result.success("发货成功", deliveryService.createDelivery(createDTO));
    }

    /**
     * 按订单号查询物流信息。
     */
    @GetMapping("/order/{orderNo}")
    public Result<Delivery> byOrderNo(@PathVariable("orderNo") String orderNo) {
        return Result.success(deliveryService.getByOrderNo(orderNo));
    }

    /**
     * 更新配送状态（模拟物流轨迹推进）。
     */
    @PutMapping("/{deliveryNo}/status")
    public Result<Void> updateStatus(@PathVariable("deliveryNo") String deliveryNo,
                                     @RequestParam("status") Integer status) {
        deliveryService.updateStatus(deliveryNo, status);
        return Result.success();
    }

    /**
     * 分页查询配送单。
     */
    @GetMapping("/page")
    public Result<PageResult<Delivery>> page(@RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                             @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                             @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(deliveryService.pageDeliveries(pageNum, pageSize, status));
    }
}
