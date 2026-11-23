package com.mall.user.controller;

import com.mall.common.result.Result;
import com.mall.user.entity.Address;
import com.mall.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址接口。
 *
 * @author 彭桂润
 */
@RestController
@RequestMapping("/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 查询用户的地址列表。
     */
    @GetMapping
    public Result<List<Address>> list(@PathVariable("userId") Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    /**
     * 新增收货地址。
     */
    @PostMapping
    public Result<Long> add(@PathVariable("userId") Long userId, @RequestBody Address address) {
        return Result.success("地址已保存", addressService.addAddress(userId, address));
    }

    /**
     * 修改收货地址。
     */
    @PutMapping("/{addressId}")
    public Result<Void> update(@PathVariable("userId") Long userId,
                               @PathVariable("addressId") Long addressId,
                               @RequestBody Address address) {
        address.setId(addressId);
        addressService.updateAddress(userId, address);
        return Result.success();
    }

    /**
     * 删除收货地址。
     */
    @DeleteMapping("/{addressId}")
    public Result<Void> remove(@PathVariable("userId") Long userId,
                               @PathVariable("addressId") Long addressId) {
        addressService.removeAddress(userId, addressId);
        return Result.success();
    }

    /**
     * 设为默认地址。
     */
    @PutMapping("/{addressId}/default")
    public Result<Void> setDefault(@PathVariable("userId") Long userId,
                                   @PathVariable("addressId") Long addressId) {
        addressService.setDefault(userId, addressId);
        return Result.success();
    }
}
