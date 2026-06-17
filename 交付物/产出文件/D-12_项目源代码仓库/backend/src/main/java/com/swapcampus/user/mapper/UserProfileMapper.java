package com.swapcampus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.user.entity.UserProfileEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfileEntity> {
}
