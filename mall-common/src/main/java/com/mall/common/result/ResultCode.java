package com.mall.common.result;

/**
 * 统一响应状态码。
 * <p>200 段为通用成功，4xx 段为参数与权限问题，5xx 段为服务端问题，
 * 1xxx 以后为各业务域自定义错误，便于前端按码做差异化提示。</p>
 *
 * @author 彭桂润
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 */
    PARAM_ERROR(400, "请求参数不合法"),
    /** 未认证 */
    UNAUTHORIZED(401, "登录状态已失效，请重新登录"),
    /** 无权限 */
    FORBIDDEN(403, "没有访问该资源的权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),
    /** 服务内部异常 */
    ERROR(500, "系统繁忙，请稍后再试"),
    /** 远程服务调用失败 */
    REMOTE_CALL_ERROR(503, "依赖服务暂时不可用"),

    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),
    /** 用户名或密码错误 */
    LOGIN_FAILED(1002, "用户名或密码错误"),
    /** 账号被禁用 */
    USER_DISABLED(1003, "账号已被禁用，请联系管理员"),

    /** 商品不存在 */
    PRODUCT_NOT_FOUND(2001, "商品不存在或已下架"),
    /** 商品已下架 */
    PRODUCT_OFF_SHELF(2002, "商品已下架"),

    /** 库存不足 */
    STOCK_NOT_ENOUGH(3001, "商品库存不足"),
    /** 库存记录不存在 */
    STOCK_NOT_FOUND(3002, "库存记录不存在"),

    /** 订单不存在 */
    ORDER_NOT_FOUND(4001, "订单不存在"),
    /** 订单状态不允许该操作 */
    ORDER_STATUS_ERROR(4002, "当前订单状态不允许该操作"),
    /** 重复提交 */
    ORDER_REPEAT_SUBMIT(4003, "请勿重复提交订单"),

    /** 支付单不存在 */
    PAYMENT_NOT_FOUND(5001, "支付单不存在"),
    /** 支付单已处理 */
    PAYMENT_ALREADY_PAID(5002, "该支付单已完成支付"),

    /** 配送单不存在 */
    DELIVERY_NOT_FOUND(6001, "配送单不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
