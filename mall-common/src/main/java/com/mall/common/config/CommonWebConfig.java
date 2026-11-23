package com.mall.common.config;

import com.mall.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 公共 Web 配置：注册用户上下文拦截器。
 *
 * @author 彭桂润
 */
@Configuration
public class CommonWebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public CommonWebConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor).addPathPatterns("/**");
    }
}
