package com.mall.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 认证所需的用户信息。
 * <p>仅在认证中心内部使用，包含密码密文，禁止在对外接口中返回。</p>
 *
 * @author 彭桂润
 */
@Data
public class UserAuthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 登录名 */
    private String username;

    /** BCrypt 密文 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 角色：ROLE_ADMIN / ROLE_USER */
    private String role;

    /** 状态：1 正常，0 禁用 */
    private Integer status;
}
