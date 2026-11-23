package com.mall.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.delivery.entity.Delivery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配送单数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface DeliveryMapper extends BaseMapper<Delivery> {
}
