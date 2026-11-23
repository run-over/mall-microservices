package com.mall.auth.config;

import com.mall.auth.security.LoginUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 令牌内容转换器。
 * <p>默认转换器只写入用户名和权限，这里补充用户 ID，
 * 使网关校验令牌后能直接拿到 userId 并透传给下游服务。</p>
 *
 * @author 彭桂润
 */
public class MallUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private static final String USER_ID = "user_id";

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(USERNAME, authentication.getName());
        if (authentication.getAuthorities() != null) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserDetails) {
            response.put(USER_ID, ((LoginUserDetails) principal).getUserId());
        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (!map.containsKey(USERNAME)) {
            return null;
        }
        Collection<? extends GrantedAuthority> authorities = null;
        Object authorityValue = map.get(AUTHORITIES);
        if (authorityValue instanceof Collection) {
            authorities = AuthorityUtils.createAuthorityList(
                    ((Collection<?>) authorityValue).toArray(new String[0]));
        }
        return new UsernamePasswordAuthenticationToken(map.get(USERNAME), "N/A", authorities);
    }
}
