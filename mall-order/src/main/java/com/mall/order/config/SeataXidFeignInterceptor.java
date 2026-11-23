package com.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 全局事务 XID 透传拦截器。
 * <p>Seata AT 模式下，全局事务 ID（XID）只存在于发起方（TM）线程的 {@link RootContext} 中。
 * 服务间通过 OpenFeign 调用时若不显式把它放进请求头，下游服务（RM）收到的就是一条普通请求，
 * 既不会注册分支事务，也不会写 undo_log —— 全局回滚时自然没有东西可回滚，
 * 表现为"订单回滚了、库存却真被扣了"的数据不一致。</p>
 * <p>使用纯 io.seata:seata-spring-boot-starter（未引入 spring-cloud-starter-alibaba-seata）时，
 * 官方不会自动为 Feign 注入 XID，必须自己补这一层。</p>
 *
 * @author 彭桂润
 */
@Configuration
public class SeataXidFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String xid = RootContext.getXID();
        if (!StringUtils.hasText(xid)) {
            return;
        }
        if (!template.headers().containsKey(RootContext.KEY_XID)) {
            template.header(RootContext.KEY_XID, xid);
        }
    }
}