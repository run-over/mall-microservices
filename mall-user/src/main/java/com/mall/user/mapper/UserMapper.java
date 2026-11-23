package com.mall.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口。
 *
 * @author 彭桂润
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
