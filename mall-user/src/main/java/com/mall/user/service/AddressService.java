package com.mall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.user.entity.Address;

import java.util.List;

/**
 * 收货地址业务接口。
 *
 * @author 彭桂润
 */
public interface AddressService extends IService<Address> {

    /**
     * 查询某用户的地址列表。
     */
    List<Address> listByUserId(Long userId);

    /**
     * 新增地址。
     */
    Long addAddress(Long userId, Address address);

    /**
     * 修改地址。
     */
    void updateAddress(Long userId, Address address);

    /**
     * 删除地址。
     */
    void removeAddress(Long userId, Long addressId);

    /**
     * 设为默认地址。
     */
    void setDefault(Long userId, Long addressId);
}
