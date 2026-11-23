package com.mall.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.api.dto.UserAuthDTO;
import com.mall.common.result.PageResult;
import com.mall.user.dto.UserQuery;
import com.mall.user.dto.UserRegisterDTO;
import com.mall.user.dto.UserUpdateDTO;
import com.mall.user.entity.User;
import com.mall.user.vo.UserVO;

/**
 * 用户业务接口。
 *
 * @author 彭桂润
 */
public interface UserService extends IService<User> {

    /**
     * 注册新用户。
     *
     * @param registerDTO 注册信息
     * @return 新用户 ID
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 分页查询用户，仅管理员可用。
     */
    PageResult<UserVO> pageUsers(UserQuery query);

    /**
     * 查询用户详情，本人或管理员可见。
     */
    UserVO getDetail(Long userId);

    /**
     * 修改用户资料。
     */
    void updateProfile(Long userId, UserUpdateDTO updateDTO);

    /**
     * 删除用户（逻辑删除），仅管理员可用。
     */
    void removeUser(Long userId);

    /**
     * 启用 / 禁用账号，仅管理员可用。
     */
    void changeStatus(Long userId, Integer status);

    /**
     * 重置密码。
     */
    void resetPassword(Long userId, String rawPassword);

    /**
     * 供认证中心按用户名拉取认证凭据。
     */
    UserAuthDTO getAuthByUsername(String username);

    /**
     * 供认证中心按 ID 拉取认证凭据。
     */
    UserAuthDTO getAuthById(Long userId);

    /**
     * 记录登录时间。
     */
    void recordLogin(Long userId);
}
