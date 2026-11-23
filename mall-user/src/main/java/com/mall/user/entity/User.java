package com.mall.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 mall_user.t_user。
 *
 * @author 彭桂润
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 登录名，全局唯一 */
    private String username;

    /** BCrypt 密码密文，禁止序列化输出 */
    @JsonIgnore
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别：1 男，2 女，0 未知 */
    private Integer gender;

    /** 角色：ROLE_ADMIN 管理员，ROLE_USER 普通用户 */
    private String role;

    /** 状态：1 正常，0 禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}
