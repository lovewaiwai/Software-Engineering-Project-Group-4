package com.swapcampus.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.auth.entity.AuthEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper extends BaseMapper<AuthEntity> {
    // TODO: Add auth SQL Server queries when business tables are implemented.
}
