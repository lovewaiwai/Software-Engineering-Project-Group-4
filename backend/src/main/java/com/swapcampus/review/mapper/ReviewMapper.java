package com.swapcampus.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.review.entity.ReviewEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<ReviewEntity> {
    // TODO: Add review SQL Server queries when business tables are implemented.
}
