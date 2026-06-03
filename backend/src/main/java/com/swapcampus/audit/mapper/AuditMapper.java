package com.swapcampus.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.audit.entity.AuditEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditMapper extends BaseMapper<AuditEntity> {
    // TODO: Add audit SQL Server queries when business tables are implemented.
}
