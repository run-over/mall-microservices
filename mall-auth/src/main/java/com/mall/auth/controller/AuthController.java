package com.mall.auth.controller;

import com.mall.common.context.UserContext;
import com.mall.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证辅助接口。
 * <p>令牌签发由 OAuth2 标准的 /oauth/token 完成，这里只补充当前用户信息与注销能力。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenStore tokenStore;

    /**
     * 当前登录用户信息（身份由网关校验令牌后透传）。
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        UserContext.LoginUser user = UserContext.getOrNull();
        if (user == null) {
            return Result.fail(com.mall.common.result.ResultCode.UNAUTHORIZED);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("roles", user.getRoles());
        return Result.success(data);
    }

    /**
     * 注销：删除数据库中的访问令牌与刷新令牌。
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = resolveToken(authorization);
        if (StringUtils.hasText(token)) {
            OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
            if (accessToken != null) {
                if (accessToken.getRefreshToken() != null) {
                    tokenStore.removeRefreshToken(accessToken.getRefreshToken());
                }
                tokenStore.removeAccessToken(accessToken);
                log.info("用户注销，令牌已从数据库移除: {}", accessToken.getValue().substring(0, 8) + "***");
            }
        }
        return Result.success("已退出登录", null);
    }

    /**
     * 从 Authorization 头中解析 Bearer 令牌。
     */
    private String resolveToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
