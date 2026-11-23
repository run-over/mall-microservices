package com.mall.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务监控台启动类。
 * <p>基于 Spring Boot Admin 汇总各微服务的健康状态、JVM 指标、日志级别与线程信息；
 * 各微服务通过 spring-boot-admin-starter-client 主动上报。</p>
 *
 * @author 彭桂润
 */
@EnableAdminServer
@EnableDiscoveryClient
@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
