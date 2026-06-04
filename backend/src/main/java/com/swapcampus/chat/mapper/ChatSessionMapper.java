package com.swapcampus.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.chat.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
