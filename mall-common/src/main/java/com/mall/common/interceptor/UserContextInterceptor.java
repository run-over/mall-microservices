package com.mall.common.interceptor;

import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户上下文拦截器。
 * <p>读取网关透传的身份请求头写入 ThreadLocal，请求结束后必须清理，
 * 否则线程池复用会造成用户信息串号。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(MallConstants.HEADER_USER_ID);
        if (StringUtils.hasText(userId)) {
            String username = request.getHeader(MallConstants.HEADER_USERNAME);
            String roles = request.getHeader(MallConstants.HEADER_USER_ROLES);
            UserContext.set(new UserContext.LoginUser(Long.valueOf(userId), username, roles));
            log.debug("当前请求上下文: userId={}, username={}, roles={}", userId, username, roles);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
