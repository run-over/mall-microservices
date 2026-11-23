package com.mall.common.context;

import com.mall.common.exception.BizException;
import com.mall.common.result.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文。
 * <p>令牌在网关统一校验，网关把用户身份写入请求头，下游服务的拦截器再把身份放入
 * ThreadLocal，业务代码即可无侵入地获取当前用户，无需重复解析令牌。</p>
 *
 * @author 彭桂润
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前用户 ID，未登录时抛出未认证异常。
     */
    public static Long requireUserId() {
        LoginUser user = HOLDER.get();
        if (user == null || user.getUserId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return user.getUserId();
    }

    /**
     * 获取当前用户，未登录返回 null（用于可选登录的场景）。
     */
    public static LoginUser getOrNull() {
        return HOLDER.get();
    }

    /**
     * 是否具备管理员角色。
     */
    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginUser {

        /** 用户 ID */
        private Long userId;

        /** 用户名 */
        private String username;

        /** 角色串，逗号分隔，如 ROLE_USER,ROLE_ADMIN */
        private String roles;
    }
}
