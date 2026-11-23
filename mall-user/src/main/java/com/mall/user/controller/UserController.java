package com.mall.user.controller;

import com.mall.api.dto.UserAuthDTO;
import com.mall.common.context.UserContext;
import com.mall.common.result.PageResult;
import com.mall.common.result.Result;
import com.mall.user.dto.UserQuery;
import com.mall.user.dto.UserRegisterDTO;
import com.mall.user.dto.UserUpdateDTO;
import com.mall.user.service.UserService;
import com.mall.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 用户服务对外接口。
 * <p>接口全部遵循 RESTful 风格，统一以 {@link Result} 包装返回。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册（无需登录，网关白名单放行）。
     */
    @PostMapping
    public Result<Long> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return Result.success("注册成功", userService.register(registerDTO));
    }

    /**
     * 查询当前登录用户信息。
     */
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(userService.getDetail(UserContext.requireUserId()));
    }

    /**
     * 分页查询用户（管理员）。
     */
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(@ModelAttribute UserQuery query) {
        return Result.success(userService.pageUsers(query));
    }

    /**
     * 查询用户详情。
     */
    @GetMapping("/{userId}")
    public Result<UserVO> detail(@PathVariable("userId") Long userId) {
        return Result.success(userService.getDetail(userId));
    }

    /**
     * 修改用户资料。
     */
    @PutMapping("/{userId}")
    public Result<Void> update(@PathVariable("userId") Long userId,
                               @Valid @RequestBody UserUpdateDTO updateDTO) {
        userService.updateProfile(userId, updateDTO);
        return Result.success();
    }

    /**
     * 启用 / 禁用账号（管理员）。
     */
    @PutMapping("/{userId}/status")
    public Result<Void> changeStatus(@PathVariable("userId") Long userId,
                                     @RequestParam("status") Integer status) {
        userService.changeStatus(userId, status);
        return Result.success();
    }

    /**
     * 重置密码（本人或管理员）。
     */
    @PutMapping("/{userId}/password")
    public Result<Void> resetPassword(@PathVariable("userId") Long userId,
                                      @RequestParam("password") String password) {
        userService.resetPassword(userId, password);
        return Result.success();
    }

    /**
     * 删除用户（管理员，逻辑删除）。
     */
    @DeleteMapping("/{userId}")
    public Result<Void> remove(@PathVariable("userId") Long userId) {
        userService.removeUser(userId);
        return Result.success();
    }

    /* ==================== 内部接口：仅供认证中心 Feign 调用 ==================== */

    /**
     * 按用户名查询认证凭据，含密码密文，不对外暴露。
     */
    @GetMapping("/internal/by-username/{username}")
    public Result<UserAuthDTO> getAuthByUsername(@PathVariable("username") String username) {
        return Result.success(userService.getAuthByUsername(username));
    }

    /**
     * 按用户 ID 查询认证凭据。
     */
    @GetMapping("/internal/{userId}")
    public Result<UserAuthDTO> getAuthById(@PathVariable("userId") Long userId) {
        return Result.success(userService.getAuthById(userId));
    }

    /**
     * 记录登录时间，由认证中心在登录成功后调用。
     */
    @PostMapping("/internal/{userId}/login-record")
    public Result<Void> recordLogin(@PathVariable("userId") Long userId) {
        userService.recordLogin(userId);
        return Result.success();
    }
}
