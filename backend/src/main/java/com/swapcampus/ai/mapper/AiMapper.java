package com.swapcampus.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.ai.entity.AiEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMapper extends BaseMapper<AiEntity> {
    // TODO: Add ai SQL Server queries when business tables are implemented.
}
