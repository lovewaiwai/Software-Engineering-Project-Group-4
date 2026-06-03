package com.swapcampus.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.search.entity.SearchEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchMapper extends BaseMapper<SearchEntity> {
    // TODO: Add search SQL Server queries when business tables are implemented.
}
