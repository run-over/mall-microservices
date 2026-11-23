package com.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 统一认证中心启动类。
 * <p>基于 Spring Security OAuth2 实现密码模式与刷新令牌模式，
 * 令牌以 JDBC 方式持久化到 mall_auth 库，网关通过 /oauth/check_token 校验令牌。</p>
 *
 * @author 彭桂润
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mall.api.feign")
@SpringBootApplication(scanBasePackages = "com.mall")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
