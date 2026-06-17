package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.TagEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<TagEntity> {
}
