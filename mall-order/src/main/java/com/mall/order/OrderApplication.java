package com.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单微服务启动类。
 * <p>本服务是 Seata 全局事务的发起方（TM），下单时通过 Feign 调用库存服务扣减库存。</p>
 *
 * @author 彭桂润
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mall.api.feign")
@MapperScan("com.mall.order.mapper")
@SpringBootApplication(scanBasePackages = "com.mall")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
