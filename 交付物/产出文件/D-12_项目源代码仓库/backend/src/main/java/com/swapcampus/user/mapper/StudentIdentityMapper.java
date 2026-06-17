package com.swapcampus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.user.entity.StudentIdentityEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentIdentityMapper extends BaseMapper<StudentIdentityEntity> {
}
