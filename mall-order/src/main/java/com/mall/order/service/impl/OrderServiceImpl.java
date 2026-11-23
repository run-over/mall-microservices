package com.mall.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.api.dto.OrderStatusDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.StockChangeDTO;
import com.mall.api.feign.DeliveryFeignClient;
import com.mall.api.feign.ProductFeignClient;
import com.mall.api.feign.StockFeignClient;
import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import com.mall.order.dto.OrderCreateDTO;
import com.mall.order.dto.OrderItemDTO;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderVO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单业务实现。
 * <p>下单是本系统唯一涉及跨库写操作的场景：订单库写入订单与明细，
 * 库存库扣减可售库存。两个库不在同一事务边界内，因此用 Seata AT 模式
 * 的 @GlobalTransactional 保证"要么都成功、要么都回滚"。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;
    private final StockFeignClient stockFeignClient;
    private final DeliveryFeignClient deliveryFeignClient;

    @Override
    @GlobalTransactional(name = "mall-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderCreateDTO createDTO) {
        Long userId = UserContext.requireUserId();
        log.info("开始创建订单: userId={}, 明细数={}", userId, createDTO.getItems().size());

        // 1. 逐行校验商品并组装明细：价格一律取服务端数据，绝不信任前端传入的金额
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO line : createDTO.getItems()) {
            Result<ProductDTO> productResult = productFeignClient.getById(line.getProductId());
            if (!productResult.isSuccess() || productResult.getData() == null) {
                throw new BizException(ResultCode.PRODUCT_NOT_FOUND,
                        "商品不存在或已下架，商品 ID: " + line.getProductId());
            }
            ProductDTO product = productResult.getData();
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BizException(ResultCode.PRODUCT_OFF_SHELF, "商品已下架: " + product.getName());
            }
            BigDecimal price = product.getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(line.getQuantity()));

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setPrice(price);
            item.setQuantity(line.getQuantity());
            item.setTotalPrice(lineTotal);
            items.add(item);
            totalAmount = totalAmount.add(lineTotal);
        }

        // 2. 写入订单主表
        Order order = new Order();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(MallConstants.ORDER_STATUS_UNPAID);
        order.setReceiver(createDTO.getReceiver());
        order.setReceiverPhone(createDTO.getReceiverPhone());
        order.setReceiverAddress(createDTO.getReceiverAddress());
        order.setRemark(createDTO.getRemark());
        this.save(order);

        // 3. 写入订单明细
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            orderItemMapper.insert(item);
        }

        // 4. 扣减库存：失败时抛出异常，Seata 会回滚上一步写入的订单与明细
        for (OrderItem item : items) {
            StockChangeDTO stockChangeDTO = new StockChangeDTO();
            stockChangeDTO.setProductId(item.getProductId());
            stockChangeDTO.setQuantity(item.getQuantity());
            stockChangeDTO.setOrderNo(order.getOrderNo());
            Result<Boolean> stockResult = stockFeignClient.deduct(stockChangeDTO);
            if (!stockResult.isSuccess()) {
                throw new BizException(ResultCode.STOCK_NOT_ENOUGH,
                        "库存扣减失败: " + item.getProductName() + "，" + stockResult.getMessage());
            }
        }

        log.info("订单创建成功: orderNo={}, amount={}", order.getOrderNo(), totalAmount);
        return order.getOrderNo();
    }

    @Override
    public PageResult<OrderVO> pageOrders(Long pageNum, Long pageSize, Integer status, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 普通用户只能看自己的订单，管理员可查全部
        if (!UserContext.isAdmin()) {
            wrapper.eq(Order::getUserId, UserContext.requireUserId());
        }
        wrapper.eq(status != null, Order::getStatus, status);
        wrapper.like(StringUtils.hasText(keyword), Order::getOrderNo, keyword);
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = this.page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), wrapper);
        return PageResult.of(page, order -> toVO(order, null));
    }

    @Override
    public OrderVO getDetail(String orderNo) {
        Order order = getByOrderNo(orderNo);
        checkOwnerOrAdmin(order);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo));
        return toVO(order, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo, String reason) {
        Order order = getByOrderNo(orderNo);
        checkOwnerOrAdmin(order);
        if (order.getStatus() != MallConstants.ORDER_STATUS_UNPAID) {
            throw new BizException(ResultCode.ORDER_STATUS_ERROR, "只有待付款订单可以取消");
        }
        order.setStatus(MallConstants.ORDER_STATUS_CANCELED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(StringUtils.hasText(reason) ? reason : "用户主动取消");
        this.updateById(order);

        // 取消后回补库存，接口本身幂等，重复调用不会多加库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo));
        for (OrderItem item : items) {
            StockChangeDTO stockChangeDTO = new StockChangeDTO();
            stockChangeDTO.setProductId(item.getProductId());
            stockChangeDTO.setQuantity(item.getQuantity());
            stockChangeDTO.setOrderNo(orderNo);
            Result<Boolean> result = stockFeignClient.release(stockChangeDTO);
            if (!result.isSuccess()) {
                log.warn("订单取消回补库存失败: orderNo={}, productId={}", orderNo, item.getProductId());
            }
        }
        log.info("订单已取消: orderNo={}, reason={}", orderNo, order.getCancelReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(OrderStatusDTO statusDTO) {
        Order order = getByOrderNo(statusDTO.getOrderNo());
        Integer target = statusDTO.getStatus();

        // 支付回调可能重复推送，已处于目标状态时直接返回，保证幂等
        if (order.getStatus().equals(target)) {
            log.info("订单状态已是目标值，忽略重复回调: orderNo={}, status={}", order.getOrderNo(), target);
            return;
        }
        if (MallConstants.ORDER_STATUS_PAID == target) {
            BizException.throwIf(order.getStatus() != MallConstants.ORDER_STATUS_UNPAID,
                    ResultCode.ORDER_STATUS_ERROR, "订单当前状态不允许支付");
            order.setPayTime(LocalDateTime.now());
        } else if (MallConstants.ORDER_STATUS_SHIPPED == target) {
            BizException.throwIf(order.getStatus() != MallConstants.ORDER_STATUS_PAID,
                    ResultCode.ORDER_STATUS_ERROR, "订单未支付，不能发货");
        } else if (MallConstants.ORDER_STATUS_FINISHED == target) {
            BizException.throwIf(order.getStatus() != MallConstants.ORDER_STATUS_SHIPPED,
                    ResultCode.ORDER_STATUS_ERROR, "订单未发货，不能确认收货");
        }
        order.setStatus(target);
        this.updateById(order);
        log.info("订单状态更新: orderNo={}, status={}", order.getOrderNo(), target);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo, String company) {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可发货");
        }
        Order order = getByOrderNo(orderNo);
        BizException.throwIf(order.getStatus() != MallConstants.ORDER_STATUS_PAID,
                ResultCode.ORDER_STATUS_ERROR, "只有已付款订单可以发货");

        DeliveryCreateDTO deliveryCreateDTO = new DeliveryCreateDTO();
        deliveryCreateDTO.setOrderNo(orderNo);
        deliveryCreateDTO.setReceiver(order.getReceiver());
        deliveryCreateDTO.setPhone(order.getReceiverPhone());
        deliveryCreateDTO.setAddress(order.getReceiverAddress());
        deliveryCreateDTO.setCompany(StringUtils.hasText(company) ? company : "顺丰速运");
        Result<String> deliveryResult = deliveryFeignClient.createDelivery(deliveryCreateDTO);
        if (!deliveryResult.isSuccess()) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR, "发货失败：" + deliveryResult.getMessage());
        }
        order.setStatus(MallConstants.ORDER_STATUS_SHIPPED);
        this.updateById(order);
        log.info("订单已发货: orderNo={}, deliveryNo={}", orderNo, deliveryResult.getData());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(String orderNo) {
        Order order = getByOrderNo(orderNo);
        checkOwnerOrAdmin(order);
        BizException.throwIf(order.getStatus() != MallConstants.ORDER_STATUS_SHIPPED,
                ResultCode.ORDER_STATUS_ERROR, "订单未发货，不能确认收货");
        order.setStatus(MallConstants.ORDER_STATUS_FINISHED);
        this.updateById(order);
    }

    /**
     * 查询订单，不存在直接抛业务异常。
     */
    private Order getByOrderNo(String orderNo) {
        Order order = this.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo), false);
        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void checkOwnerOrAdmin(Order order) {
        Long currentUserId = UserContext.requireUserId();
        if (!currentUserId.equals(order.getUserId()) && !UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作他人订单");
        }
    }

    /**
     * 生成订单号：时间戳 + 用户尾号 + 随机数，数据库唯一索引兜底防重。
     */
    private String generateOrderNo(Long userId) {
        String timePart = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        String userPart = String.format("%04d", userId % 10000);
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "MO" + timePart + userPart + randomPart;
    }

    private OrderVO toVO(Order order, List<OrderItem> items) {
        OrderVO vo = BeanUtil.copyProperties(order, OrderVO.class);
        vo.setItems(items == null ? new ArrayList<>() : items);
        vo.setStatusDesc(statusDesc(order.getStatus()));
        return vo;
    }

    private String statusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case MallConstants.ORDER_STATUS_UNPAID:
                return "待付款";
            case MallConstants.ORDER_STATUS_PAID:
                return "已付款";
            case MallConstants.ORDER_STATUS_SHIPPED:
                return "已发货";
            case MallConstants.ORDER_STATUS_FINISHED:
                return "已完成";
            case MallConstants.ORDER_STATUS_CANCELED:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
