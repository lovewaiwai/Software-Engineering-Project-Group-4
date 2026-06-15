package com.swapcampus.chat.service;

import com.swapcampus.chat.dto.CreateSessionRequest;
import com.swapcampus.chat.dto.SendMessageRequest;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.vo.ChatSessionResponse;

import java.util.List;

public interface ChatService {

    ChatSessionResponse createOrGetSession(Long userId, CreateSessionRequest request);

    List<ChatSessionResponse> listSessions(Long userId);

    List<ChatMessageResponse> listMessages(Long userId, Long sessionId, int limit);

    ChatMessageResponse sendMessage(Long userId, Long sessionId, SendMessageRequest request);

    List<ChatMessageResponse> markSessionRead(Long userId, Long sessionId);

    ChatMessageResponse recallMessage(Long userId, Long messageId);
}
