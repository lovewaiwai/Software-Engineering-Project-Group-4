package com.swapcampus.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.chat.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {

    @Select("SELECT ISNULL(MAX(seq_no), 0) FROM chat_messages WHERE session_id = #{sessionId}")
    Long selectMaxSeqNo(@Param("sessionId") Long sessionId);
}
