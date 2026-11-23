package com.mall.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户微服务启动类。
 *
 * @author 彭桂润
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mall.api.feign")
@MapperScan("com.mall.user.mapper")
@SpringBootApplication(scanBasePackages = "com.mall")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
