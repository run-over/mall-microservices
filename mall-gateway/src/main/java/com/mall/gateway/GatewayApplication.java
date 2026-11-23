package com.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动类。
 * <p>网关是系统唯一入口，负责路由转发、令牌校验、身份透传与入口限流。
 * 注意：网关基于 WebFlux，不能引入 spring-boot-starter-web，否则会启动失败。</p>
 *
 * @author 彭桂润
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
