package com.mall.auth.config;

import com.mall.auth.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * OAuth2 授权服务器配置。
 * <p>要点：
 * <ul>
 *   <li>令牌存储使用 {@link JdbcTokenStore}，访问令牌与刷新令牌全部落库，重启不丢失；</li>
 *   <li>客户端信息使用 {@link JdbcClientDetailsService}，由 oauth_client_details 表维护；</li>
 *   <li>支持 password 与 refresh_token 两种授权模式，供前后端分离场景使用；</li>
 *   <li>自定义令牌转换器，把 userId 写入令牌附加信息。</li>
 * </ul></p>
 *
 * @author 彭桂润
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final DataSource dataSource;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * 延迟注入，避免授权服务器与 Spring Security 配置之间的循环依赖。
     */
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthorizationServerConfig(DataSource dataSource, UserDetailsServiceImpl userDetailsService) {
        this.dataSource = dataSource;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 令牌存储：JDBC 实现，令牌持久化到数据库。
     */
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * 客户端详情服务：从 oauth_client_details 表读取。
     */
    @Bean
    public ClientDetailsService clientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 令牌内容转换器：默认内容 + 自定义 userId。
     */
    @Bean
    public AccessTokenConverter accessTokenConverter() {
        DefaultAccessTokenConverter converter = new DefaultAccessTokenConverter();
        converter.setUserTokenConverter(new MallUserAuthenticationConverter());
        return converter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter())
                // 每次刷新都签发新的刷新令牌，降低刷新令牌被长期复用的风险
                .reuseRefreshTokens(false)
                .allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.GET);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // 校验令牌端点允许匿名访问，由网关携带 client 信息调用
                .checkTokenAccess("permitAll()")
                .tokenKeyAccess("permitAll()")
                // 允许表单方式提交 client_id / client_secret
                .allowFormAuthenticationForClients();
    }
}
