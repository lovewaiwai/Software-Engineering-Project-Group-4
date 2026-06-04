package com.swapcampus.chat.controller;

import com.swapcampus.chat.dto.CreateSessionRequest;
import com.swapcampus.chat.dto.SendMessageRequest;
import com.swapcampus.chat.service.ChatService;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.vo.ChatSessionResponse;
import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.security.CurrentUserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("module", chatService.moduleName(), "status", "ok"));
    }

    @PostMapping("/sessions")
    public ApiResponse<ChatSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.createOrGetSession(userId, request));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionResponse>> listSessions() {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.listSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessageResponse>> listMessages(@PathVariable Long sessionId,
                                                               @RequestParam(defaultValue = "30") int limit) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.listMessages(userId, sessionId, limit));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<ChatMessageResponse> sendMessage(@PathVariable Long sessionId,
                                                        @Valid @RequestBody SendMessageRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.sendMessage(userId, sessionId, request));
    }

    @PostMapping("/sessions/{sessionId}/read")
    public ApiResponse<List<ChatMessageResponse>> markRead(@PathVariable Long sessionId) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.markSessionRead(userId, sessionId));
    }

    @PostMapping("/messages/{messageId}/recall")
    public ApiResponse<ChatMessageResponse> recallMessage(@PathVariable Long messageId) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(chatService.recallMessage(userId, messageId));
    }
}
