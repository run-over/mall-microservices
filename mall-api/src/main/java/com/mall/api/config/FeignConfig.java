package com.mall.api.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Feign 配置。
 * <p>日志级别设为 FULL，可完整打印请求头、请求体与响应体，便于排查跨服务调用问题；
 * 关闭默认重试器，避免非幂等接口（如下单扣库存）被重复执行。</p>
 *
 * @author 彭桂润
 */
@Configuration
public class FeignConfig {

    /**
     * 日志级别：FULL 输出请求与响应的完整内容。
     * <p>注意：仅在开发生效，生产环境应降为 BASIC 并通过日志级别控制输出。</p>
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 禁用重试：下单、扣库存等写操作重试可能造成重复扣减，
     * 幂等性交由业务层（订单号唯一索引 + 唯一流水）保证。
     */
    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
