package com.swapcampus.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.recommend.entity.RecommendEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecommendMapper extends BaseMapper<RecommendEntity> {
    // TODO: Add recommend SQL Server queries when business tables are implemented.
}
