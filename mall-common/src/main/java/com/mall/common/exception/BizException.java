package com.mall.common.exception;

import com.mall.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 * <p>业务校验失败时抛出该异常，由 {@link GlobalExceptionHandler} 统一转换为标准响应，
 * 避免在 Controller 中大量编写 if-else 的返回值拼装。</p>
 *
 * @author 彭桂润
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 条件成立时抛出异常，简化前置校验代码。
     */
    public static void throwIf(boolean condition, ResultCode resultCode) {
        if (condition) {
            throw new BizException(resultCode);
        }
    }

    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BizException(message);
        }
    }

    public static void throwIf(boolean condition, ResultCode resultCode, String message) {
        if (condition) {
            throw new BizException(resultCode, message);
        }
    }
}
