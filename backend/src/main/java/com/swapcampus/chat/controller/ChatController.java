package com.swapcampus.chat.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.chat.service.ChatService;
import com.swapcampus.chat.vo.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/health")
    public ApiResponse<ChatResponse> health() {
        return ApiResponse.ok(ChatResponse.placeholder(chatService.moduleName()));
    }
}
