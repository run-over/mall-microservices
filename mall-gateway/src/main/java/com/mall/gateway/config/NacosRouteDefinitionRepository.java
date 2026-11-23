package com.mall.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 基于 Nacos 的动态路由仓库。
 * <p>路由配置存放在 Nacos 的 mall-gateway-routes.json 中，网关启动时拉取，
 * 并注册监听器；配置修改后无需重启网关即可生效（发布 RefreshRoutesEvent 触发热更新）。
 * <p>这样新增一个微服务只需要在 Nacos 增加一条路由，不必改代码重新打包。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Component
@RefreshScope
public class NacosRouteDefinitionRepository implements RouteDefinitionRepository {

    private static final String DATA_ID = "mall-gateway-routes.json";

    private final NacosConfigManager nacosConfigManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApplicationEventPublisher publisher;

    @Value("${mall.gateway.route-group:MALL_GROUP}")
    private String group;

    /** 路由列表，配置变更时整体替换，读多写少用 volatile 保证可见性 */
    private volatile List<RouteDefinition> routes = Collections.emptyList();

    public NacosRouteDefinitionRepository(NacosConfigManager nacosConfigManager,
                                          ApplicationEventPublisher publisher) {
        this.nacosConfigManager = nacosConfigManager;
        this.publisher = publisher;
    }

    @PostConstruct
    public void init() {
        ConfigService configService = nacosConfigManager.getConfigService();
        loadRoutes(configService);
        try {
            configService.addListener(DATA_ID, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("检测到 Nacos 路由配置变更，开始热更新");
                    parseRoutes(configInfo);
                    publisher.publishEvent(new RefreshRoutesEvent(this));
                }
            });
        } catch (Exception e) {
            log.error("注册 Nacos 路由监听失败: {}", e.getMessage());
        }
    }

    /**
     * 拉取并解析路由配置。
     */
    private void loadRoutes(ConfigService configService) {
        try {
            String configInfo = configService.getConfig(DATA_ID, group, 5000);
            if (!StringUtils.hasText(configInfo)) {
                log.warn("未获取到 Nacos 动态路由配置: dataId={}, group={}，将仅使用本地静态路由", DATA_ID, group);
                return;
            }
            parseRoutes(configInfo);
        } catch (Exception e) {
            log.error("加载 Nacos 动态路由失败: {}", e.getMessage());
        }
    }

    private void parseRoutes(String configInfo) {
        if (!StringUtils.hasText(configInfo)) {
            return;
        }
        try {
            List<RouteDefinition> definitions = objectMapper.readValue(
                    configInfo, new TypeReference<List<RouteDefinition>>() {
                    });
            this.routes = definitions;
            log.info("Nacos 动态路由加载完成，共 {} 条", definitions.size());
        } catch (Exception e) {
            log.error("解析 Nacos 路由配置失败，保留原有路由: {}", e.getMessage());
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes);
    }

    @Override
    public reactor.core.publisher.Mono<Void> save(reactor.core.publisher.Mono<RouteDefinition> route) {
        // 本系统不支持通过接口写路由，路由统一由 Nacos 配置维护
        return reactor.core.publisher.Mono.empty();
    }

    @Override
    public reactor.core.publisher.Mono<Void> delete(reactor.core.publisher.Mono<String> routeId) {
        return reactor.core.publisher.Mono.empty();
    }
}
