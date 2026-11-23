package com.mall.user.dto;

import lombok.Data;

/**
 * 用户分页查询条件。
 *
 * @author 彭桂润
 */
@Data
public class UserQuery {

    /** 页码，从 1 开始 */
    private Long pageNum = 1L;

    /** 每页条数 */
    private Long pageSize = 10L;

    /** 关键字：匹配用户名、昵称或手机号 */
    private String keyword;

    /** 角色过滤 */
    private String role;

    /** 状态过滤 */
    private Integer status;
}
