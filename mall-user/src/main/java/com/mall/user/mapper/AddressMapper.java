package com.mall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.user.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
