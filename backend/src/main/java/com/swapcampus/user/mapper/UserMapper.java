package com.swapcampus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // TODO: Add user SQL Server queries when business tables are implemented.
}
