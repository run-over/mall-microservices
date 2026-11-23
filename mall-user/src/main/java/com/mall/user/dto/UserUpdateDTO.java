package com.mall.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

/**
 * 用户资料修改请求。
 *
 * @author 彭桂润
 */
@Data
public class UserUpdateDTO {

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 性别 */
    private Integer gender;
}
