package com.mall.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mall.api.dto.OrderStatusDTO;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import com.mall.order.dto.OrderCreateDTO;
import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 订单接口。
 * <p>下单接口标注 @SentinelResource("createOrder")，配合流控规则做入口限流，
 * 被限流时返回友好提示而不是线程池被打满。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单（核心链路：Seata 全局事务 + Sentinel 流控保护）。
     */
    @PostMapping
    @SentinelResource(value = "createOrder", blockHandler = "createOrderBlocked")
    public Result<String> create(@Valid @RequestBody OrderCreateDTO createDTO) {
        return Result.success("下单成功", orderService.createOrder(createDTO));
    }

    /**
     * 下单被限流后的兜底方法。
     */
    public Result<String> createOrderBlocked(OrderCreateDTO createDTO, BlockException ex) {
        log.warn("下单请求被限流: rule={}", ex.getRule());
        return Result.fail(ResultCode.ERROR, "当前下单人数过多，请稍后重试");
    }

    /**
     * 分页查询订单。
     */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(@RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                            @RequestParam(value = "status", required = false) Integer status,
                                            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(orderService.pageOrders(pageNum, pageSize, status, keyword));
    }

    /**
     * 订单详情。
     */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable("orderNo") String orderNo) {
        return Result.success(orderService.getDetail(orderNo));
    }

    /**
     * 取消订单。
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable("orderNo") String orderNo,
                               @RequestParam(value = "reason", required = false) String reason) {
        orderService.cancelOrder(orderNo, reason);
        return Result.success();
    }

    /**
     * 发货（管理员）。
     */
    @PutMapping("/{orderNo}/ship")
    public Result<Void> ship(@PathVariable("orderNo") String orderNo,
                             @RequestParam(value = "company", required = false) String company) {
        orderService.shipOrder(orderNo, company);
        return Result.success();
    }

    /**
     * 确认收货。
     */
    @PutMapping("/{orderNo}/finish")
    public Result<Void> finish(@PathVariable("orderNo") String orderNo) {
        orderService.confirmReceipt(orderNo);
        return Result.success();
    }

    /**
     * 更新订单状态，供支付、配送服务回调。
     */
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody OrderStatusDTO statusDTO) {
        orderService.updateStatus(statusDTO);
        return Result.success();
    }
}
