package com.mall.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 认证主体。
 * <p>在标准 UserDetails 之外额外携带用户 ID，令牌签发时写入附加信息，
 * 网关校验令牌后即可直接透传用户 ID，无需再查一次用户服务。</p>
 *
 * @author 彭桂润
 */
@Getter
public class LoginUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private final Long userId;

    private final String username;

    private final String password;

    /** 角色，形如 ROLE_USER */
    private final String role;

    /** 状态：1 正常，0 禁用 */
    private final Integer status;

    public LoginUserDetails(Long userId, String username, String password, String role, Integer status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
