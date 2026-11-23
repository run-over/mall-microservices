package com.mall.auth.security;

import com.mall.api.dto.UserAuthDTO;
import com.mall.api.feign.UserFeignClient;
import com.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户凭据加载服务。
 * <p>认证中心不直接连用户库，而是通过 Feign 调用用户服务获取凭据，
 * 保证认证逻辑与用户数据归属解耦。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserFeignClient userFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<UserAuthDTO> result = userFeignClient.getByUsername(username);
        if (!result.isSuccess() || result.getData() == null) {
            log.warn("登录失败，用户不存在: {}", username);
            // 统一提示"用户名或密码错误"，避免暴露账号是否存在
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        UserAuthDTO user = result.getData();
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new DisabledException("账号已被禁用，请联系管理员");
        }
        log.info("用户凭据加载成功: userId={}, username={}, role={}", user.getUserId(), user.getUsername(), user.getRole());
        return new LoginUserDetails(user.getUserId(), user.getUsername(), user.getPassword(),
                user.getRole(), user.getStatus());
    }
}
