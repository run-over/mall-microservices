package com.mall.common.constant;

/**
 * 全局常量。
 * <p>集中定义网关透传请求头、角色、订单与支付状态等跨服务共享的字面量，
 * 避免各服务硬编码字符串造成不一致。</p>
 *
 * @author 彭桂润
 */
public final class MallConstants {

    private MallConstants() {
    }

    /* ==================== 网关透传请求头 ==================== */

    /** 用户 ID 请求头（由网关校验令牌后写入） */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 用户名请求头 */
    public static final String HEADER_USERNAME = "X-Username";

    /** 用户角色请求头 */
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    /** 链路追踪 ID 请求头（Sleuth 自动写入，此处仅作日志打印用） */
    public static final String HEADER_TRACE_ID = "X-B3-TraceId";

    /* ==================== 角色 ==================== */

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String ROLE_USER = "ROLE_USER";

    /* ==================== 订单状态 ==================== */

    /** 待支付 */
    public static final int ORDER_STATUS_UNPAID = 0;
    /** 已支付 */
    public static final int ORDER_STATUS_PAID = 1;
    /** 已发货 */
    public static final int ORDER_STATUS_SHIPPED = 2;
    /** 已完成 */
    public static final int ORDER_STATUS_FINISHED = 3;
    /** 已取消 */
    public static final int ORDER_STATUS_CANCELED = 4;

    /* ==================== 支付状态 ==================== */

    public static final int PAY_STATUS_WAITING = 0;
    public static final int PAY_STATUS_SUCCESS = 1;
    public static final int PAY_STATUS_CLOSED = 2;

    /* ==================== 库存流水类型 ==================== */

    public static final int STOCK_LOG_DEDUCT = 1;
    public static final int STOCK_LOG_RELEASE = 2;

    /* ==================== 配送状态 ==================== */

    public static final int DELIVERY_STATUS_CREATED = 0;
    public static final int DELIVERY_STATUS_TRANSIT = 1;
    public static final int DELIVERY_STATUS_SIGNED = 2;
}
