package com.mall.api.feign;

import com.mall.api.dto.UserAuthDTO;
import com.mall.api.fallback.UserFeignFallbackFactory;
import com.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务调用客户端。
 * <p>认证中心通过它拉取用户凭据完成密码校验，避免认证服务直接访问用户库，
 * 保证"一个微服务只拥有一个数据库"的边界约束。</p>
 *
 * @author 彭桂润
 */
@FeignClient(name = "mall-user", path = "/users", fallbackFactory = UserFeignFallbackFactory.class)
public interface UserFeignClient {

    /**
     * 按登录名查询认证凭据。
     *
     * @param username 登录名
     * @return 含密码密文的用户信息
     */
    @GetMapping("/internal/by-username/{username}")
    Result<UserAuthDTO> getByUsername(@PathVariable("username") String username);

    /**
     * 按用户 ID 查询认证凭据。
     *
     * @param userId 用户 ID
     * @return 含密码密文的用户信息
     */
    @GetMapping("/internal/{userId}")
    Result<UserAuthDTO> getById(@PathVariable("userId") Long userId);
}
