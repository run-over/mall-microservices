package com.mall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.ResultCode;
import com.mall.user.entity.Address;
import com.mall.user.mapper.AddressMapper;
import com.mall.user.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址业务实现。
 *
 * @author 彭桂润
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    /** 单用户地址上限，防止恶意刷数据 */
    private static final int MAX_ADDRESS_COUNT = 20;

    @Override
    public List<Address> listByUserId(Long userId) {
        checkSelfOrAdmin(userId);
        return this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(Long userId, Address address) {
        checkSelfOrAdmin(userId);
        long count = this.count(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        BizException.throwIf(count >= MAX_ADDRESS_COUNT, "收货地址数量已达上限");

        address.setId(null);
        address.setUserId(userId);
        // 第一条地址自动成为默认地址
        if (count == 0) {
            address.setIsDefault(1);
        } else if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefault(userId);
        } else {
            address.setIsDefault(0);
        }
        this.save(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long userId, Address address) {
        Address exists = getOwned(userId, address.getId());
        exists.setReceiver(address.getReceiver());
        exists.setPhone(address.getPhone());
        exists.setProvince(address.getProvince());
        exists.setCity(address.getCity());
        exists.setDistrict(address.getDistrict());
        exists.setDetail(address.getDetail());
        this.updateById(exists);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAddress(Long userId, Long addressId) {
        getOwned(userId, addressId);
        this.removeById(addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long addressId) {
        getOwned(userId, addressId);
        clearDefault(userId);
        Address address = new Address();
        address.setId(addressId);
        address.setIsDefault(1);
        this.updateById(address);
    }

    /**
     * 清除该用户的其他默认地址。
     */
    private void clearDefault(Long userId) {
        this.update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0));
    }

    /**
     * 校验地址归属，防止横向越权。
     */
    private Address getOwned(Long userId, Long addressId) {
        checkSelfOrAdmin(userId);
        Address address = this.getById(addressId);
        BizException.throwIf(address == null, "收货地址不存在");
        BizException.throwIf(!userId.equals(address.getUserId()), ResultCode.FORBIDDEN, "无权操作他人地址");
        return address;
    }

    private void checkSelfOrAdmin(Long userId) {
        Long currentUserId = UserContext.requireUserId();
        if (!currentUserId.equals(userId) && !UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "无权访问其他用户的地址");
        }
    }
}
