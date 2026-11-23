package com.mall.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.DeliveryCreateDTO;
import com.mall.common.constant.MallConstants;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.ResultCode;
import com.mall.delivery.entity.Delivery;
import com.mall.delivery.mapper.DeliveryMapper;
import com.mall.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 配送业务实现。
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, Delivery> implements DeliveryService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createDelivery(DeliveryCreateDTO createDTO) {
        BizException.throwIf(!StringUtils.hasText(createDTO.getOrderNo()), "订单号不能为空");

        // 幂等：一单一配送，重复发货直接返回已有配送单
        Delivery exists = this.getOne(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getOrderNo, createDTO.getOrderNo()), false);
        if (exists != null) {
            log.info("该订单已存在配送单，直接返回: orderNo={}, deliveryNo={}",
                    createDTO.getOrderNo(), exists.getDeliveryNo());
            return exists.getDeliveryNo();
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryNo("DL" + LocalDateTime.now().format(NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 9999));
        delivery.setOrderNo(createDTO.getOrderNo());
        delivery.setCompany(StringUtils.hasText(createDTO.getCompany()) ? createDTO.getCompany() : "顺丰速运");
        delivery.setTrackingNo("SF" + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(100, 999));
        delivery.setReceiver(createDTO.getReceiver());
        delivery.setPhone(createDTO.getPhone());
        delivery.setAddress(createDTO.getAddress());
        delivery.setStatus(MallConstants.DELIVERY_STATUS_CREATED);
        this.save(delivery);
        log.info("配送单已创建: deliveryNo={}, orderNo={}", delivery.getDeliveryNo(), delivery.getOrderNo());
        return delivery.getDeliveryNo();
    }

    @Override
    public Delivery getByOrderNo(String orderNo) {
        Delivery delivery = this.getOne(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getOrderNo, orderNo), false);
        if (delivery == null) {
            throw new BizException(ResultCode.DELIVERY_NOT_FOUND);
        }
        return delivery;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String deliveryNo, Integer status) {
        Delivery delivery = this.getOne(new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getDeliveryNo, deliveryNo), false);
        if (delivery == null) {
            throw new BizException(ResultCode.DELIVERY_NOT_FOUND);
        }
        delivery.setStatus(status);
        if (status != null && status == MallConstants.DELIVERY_STATUS_SIGNED) {
            delivery.setSignTime(LocalDateTime.now());
        }
        this.updateById(delivery);
        log.info("配送状态更新: deliveryNo={}, status={}", deliveryNo, status);
    }

    @Override
    public PageResult<Delivery> pageDeliveries(Long pageNum, Long pageSize, Integer status) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Delivery::getStatus, status);
        wrapper.orderByDesc(Delivery::getCreateTime);
        Page<Delivery> page = this.page(new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize), wrapper);
        return PageResult.of(page);
    }
}
