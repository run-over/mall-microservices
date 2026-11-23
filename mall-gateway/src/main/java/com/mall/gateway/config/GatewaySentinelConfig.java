package com.mall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关限流兜底配置。
 * <p>Sentinel 对网关做流控时，被限流的请求不会进入业务服务，
 * 这里自定义返回结构，保证前端收到的仍是统一 JSON 格式。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler handler = (exchange, throwable) -> {
            log.warn("网关请求被限流: path={}", exchange.getRequest().getURI().getPath());
            Map<String, Object> body = new HashMap<>();
            body.put("code", 429);
            body.put("message", "访问过于频繁，请稍后再试");
            body.put("data", null);
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body));
        };
        GatewayCallbackManager.setBlockHandler(handler);
    }
}
