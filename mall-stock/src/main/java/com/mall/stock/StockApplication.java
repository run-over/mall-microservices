package com.mall.stock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 库存微服务启动类。
 * <p>本服务作为 Seata 的 RM（资源管理器）参与订单的全局事务，
 * 通过 seata.enable-auto-data-source-proxy=true 自动代理数据源生成 undo_log。</p>
 *
 * @author 彭桂润
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mall.api.feign")
@MapperScan("com.mall.stock.mapper")
@SpringBootApplication(scanBasePackages = "com.mall")
public class StockApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockApplication.class, args);
    }
}
