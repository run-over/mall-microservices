package com.mall.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.constant.MallConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一鉴权过滤器。
 * <p>流程：白名单放行 → 取令牌 → 调用认证中心 /oauth/check_token 校验 →
 * 校验通过后把 userId、username、roles 写入请求头并转发。
 * <p>安全要点：先删除客户端可能伪造的同名请求头，再写入服务端可信值，
 * 否则外部请求可以直接携带 X-User-Id 冒充他人。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 完全放行的路径（登录、注册等） */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/oauth/token",
            "/api/auth/oauth/check_token",
            "/api/user/users"
    );

    /** 允许匿名访问的只读路径前缀 */
    private static final List<String> OPEN_GET_PREFIX = Arrays.asList(
            "/api/product/products",
            "/api/product/categories"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 认证中心令牌校验地址，通过 Nacos 配置注入，默认指向认证服务 */
    @Value("${mall.auth.check-token-url:http://127.0.0.1:9000/oauth/check_token}")
    private String checkTokenUrl;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isWhiteList(request, path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少令牌: {} {}", request.getMethod(), path);
            return unauthorized(exchange, "缺少访问令牌，请先登录");
        }

        return webClient.post()
                .uri(checkTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("token", token))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(body -> {
                    if (body == null || !Boolean.TRUE.equals(body.get("active"))) {
                        return unauthorized(exchange, "令牌无效或已过期，请重新登录");
                    }
                    return chain.filter(exchange.mutate().request(withUserHeaders(request, body)).build());
                })
                // 认证中心不可用时按未认证处理，避免把请求直接放行
                .onErrorResume(e -> {
                    log.error("令牌校验失败: {}", e.getMessage());
                    return unauthorized(exchange, "认证服务暂时不可用，请稍后重试");
                });
    }

    /**
     * 把令牌中的身份信息写入请求头，供下游服务使用。
     */
    @SuppressWarnings("unchecked")
    private ServerHttpRequest withUserHeaders(ServerHttpRequest request, Map<String, Object> body) {
        Object userId = body.get("user_id");
        String username = body.get("user_name") == null ? "" : String.valueOf(body.get("user_name"));
        Collection<String> authorities = (Collection<String>) body.get("authorities");
        String roles = authorities == null ? "" : String.join(",", authorities);

        return request.mutate()
                .headers(headers -> {
                    // 先清理客户端可能伪造的身份头
                    headers.remove(MallConstants.HEADER_USER_ID);
                    headers.remove(MallConstants.HEADER_USERNAME);
                    headers.remove(MallConstants.HEADER_USER_ROLES);
                    if (userId != null) {
                        headers.add(MallConstants.HEADER_USER_ID, String.valueOf(userId));
                    }
                    headers.add(MallConstants.HEADER_USERNAME, username);
                    headers.add(MallConstants.HEADER_USER_ROLES, roles);
                })
                .build();
    }

    /**
     * 白名单判断：登录、注册等接口无需令牌，商品与类目的查询接口允许游客浏览。
     */
    private boolean isWhiteList(ServerHttpRequest request, String path) {
        if ("/actuator/health".equals(path)) {
            return true;
        }
        for (String pattern : WHITE_LIST) {
            if (PATH_MATCHER.match(pattern, path)) {
                // 注册接口只放行 POST，避免误放开用户列表查询
                if (path.startsWith("/api/user/users")) {
                    return HttpMethod.POST.equals(request.getMethod());
                }
                return true;
            }
        }
        if (HttpMethod.GET.equals(request.getMethod())) {
            for (String prefix : OPEN_GET_PREFIX) {
                if (PATH_MATCHER.match(prefix + "/**", path) || path.equals(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从 Authorization 头解析 Bearer 令牌。
     */
    private String resolveToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 返回 401 JSON，与业务接口保持一致的响应结构。
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("data", null);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] bytes = ("{\"code\":401,\"message\":\"未认证\"}").getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    @Override
    public int getOrder() {
        // 数值越小越先执行，鉴权必须在转发之前
        return -100;
    }
}
