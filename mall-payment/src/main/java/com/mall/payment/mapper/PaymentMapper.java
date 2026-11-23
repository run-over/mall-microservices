package com.mall.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
