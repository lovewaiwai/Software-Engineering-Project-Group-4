package com.swapcampus.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.chat.entity.ChatEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMapper extends BaseMapper<ChatEntity> {
    // TODO: Add chat SQL Server queries when business tables are implemented.
}
