package com.mall.api.config;

import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Feign 身份透传拦截器。
 * <p>服务间调用不经过网关，若不透传身份请求头，下游服务将拿不到当前用户。
 * 这里从 ThreadLocal 取出身份并写入 Feign 请求头，保证跨服务调用链上的用户上下文连续。</p>
 *
 * @author 彭桂润
 */
@Configuration
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        UserContext.LoginUser user = UserContext.getOrNull();
        if (user == null || user.getUserId() == null) {
            return;
        }
        template.header(MallConstants.HEADER_USER_ID, String.valueOf(user.getUserId()));
        if (StringUtils.hasText(user.getUsername())) {
            template.header(MallConstants.HEADER_USERNAME, user.getUsername());
        }
        if (StringUtils.hasText(user.getRoles())) {
            template.header(MallConstants.HEADER_USER_ROLES, user.getRoles());
        }
    }
}
