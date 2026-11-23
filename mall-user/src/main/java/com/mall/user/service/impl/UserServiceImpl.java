package com.mall.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.UserAuthDTO;
import com.mall.common.constant.MallConstants;
import com.mall.common.context.UserContext;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.result.ResultCode;
import com.mall.common.util.PasswordUtil;
import com.mall.user.dto.UserQuery;
import com.mall.user.dto.UserRegisterDTO;
import com.mall.user.dto.UserUpdateDTO;
import com.mall.user.entity.User;
import com.mall.user.mapper.UserMapper;
import com.mall.user.service.UserService;
import com.mall.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务实现。
 * <p>权限判断放在服务层而非网关，原因是网关只做"是否登录"的粗粒度校验，
 * 而"能否操作这条数据"属于业务规则，必须由数据所属服务裁决。</p>
 *
 * @author 彭桂润
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        BizException.throwIf(count > 0, "该用户名已被注册");

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname())
                ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setGender(registerDTO.getGender() == null ? 0 : registerDTO.getGender());
        // 注册接口只允许创建普通用户，管理员账号由初始化脚本预置
        user.setRole(MallConstants.ROLE_USER);
        user.setStatus(1);
        this.save(user);
        log.info("新用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    public PageResult<UserVO> pageUsers(UserQuery query) {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可查询用户列表");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.eq(StringUtils.hasText(query.getRole()), User::getRole, query.getRole());
        wrapper.eq(query.getStatus() != null, User::getStatus, query.getStatus());
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page, user -> BeanUtil.copyProperties(user, UserVO.class));
    }

    @Override
    public UserVO getDetail(Long userId) {
        checkSelfOrAdmin(userId);
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserUpdateDTO updateDTO) {
        checkSelfOrAdmin(userId);
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (StringUtils.hasText(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        user.setPhone(updateDTO.getPhone());
        user.setEmail(updateDTO.getEmail());
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可删除用户");
        }
        BizException.throwIf(userId.equals(UserContext.requireUserId()), "不能删除当前登录账号");
        BizException.throwIf(this.getById(userId) == null, ResultCode.USER_NOT_FOUND);
        this.removeById(userId);
        log.info("用户已被删除: userId={}, operator={}", userId, UserContext.requireUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, Integer status) {
        if (!UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "仅管理员可启用或禁用账号");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String rawPassword) {
        checkSelfOrAdmin(userId);
        BizException.throwIf(!StringUtils.hasText(rawPassword) || rawPassword.length() < 6, "新密码长度不能少于 6 位");
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(PasswordUtil.encode(rawPassword));
        this.updateById(user);
        log.info("密码已重置: userId={}", userId);
    }

    @Override
    public UserAuthDTO getAuthByUsername(String username) {
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username), false);
        return toAuthDTO(user);
    }

    @Override
    public UserAuthDTO getAuthById(Long userId) {
        return toAuthDTO(this.getById(userId));
    }

    @Override
    public void recordLogin(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
    }

    /**
     * 实体转换成认证凭据。
     */
    private UserAuthDTO toAuthDTO(User user) {
        if (user == null) {
            return null;
        }
        UserAuthDTO dto = new UserAuthDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setNickname(user.getNickname());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }

    /**
     * 本人或管理员才允许访问该用户数据。
     */
    private void checkSelfOrAdmin(Long userId) {
        Long currentUserId = UserContext.requireUserId();
        if (!currentUserId.equals(userId) && !UserContext.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "无权访问其他用户的数据");
        }
    }
}
