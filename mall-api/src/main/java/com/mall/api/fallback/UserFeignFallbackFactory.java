package com.mall.api.fallback;

import com.mall.api.dto.UserAuthDTO;
import com.mall.api.feign.UserFeignClient;
import com.mall.common.result.Result;
import com.mall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务降级工厂。
 * <p>用户服务不可用时返回统一失败结果，认证中心据此提示"暂时无法登录"，
 * 而不是把底层连接异常暴露给调用方。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Component
public class UserFeignFallbackFactory implements FallbackFactory<UserFeignClient> {

    @Override
    public UserFeignClient create(Throwable cause) {
        log.error("调用用户服务失败，已触发降级: {}", cause.getMessage());
        return new UserFeignClient() {
            @Override
            public Result<UserAuthDTO> getByUsername(String username) {
                return Result.fail(ResultCode.REMOTE_CALL_ERROR, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<UserAuthDTO> getById(Long userId) {
                return Result.fail(ResultCode.REMOTE_CALL_ERROR, "用户服务暂时不可用，请稍后重试");
            }
        };
    }
}
