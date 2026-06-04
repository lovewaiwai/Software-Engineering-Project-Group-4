package com.swapcampus.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.admin.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<AdminEntity> {
    // TODO: Add admin SQL Server queries when business tables are implemented.
}
