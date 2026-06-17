package com.swapcampus.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapcampus.chat.dto.SendMessageRequest;
import com.swapcampus.chat.service.impl.ChatServiceImpl;
import com.swapcampus.chat.websocket.ChatWebSocketAuthInterceptor;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.MessageType;
import com.swapcampus.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatServiceImpl chatService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatServiceImpl chatService,
                                ChatWebSocketSessionRegistry sessionRegistry,
                                ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = userId(session);
        if (userId != null) {
            sessionRegistry.register(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = userId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        JsonNode root = objectMapper.readTree(message.getPayload());
        String type = root.path("type").asText();
        try {
            switch (type) {
                case "CHAT_MESSAGE" -> handleChatMessage(userId, root);
                case "READ_RECEIPT" -> handleReadReceipt(userId, root);
                default -> session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"unsupported type\"}"));
            }
        } catch (BusinessException ex) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "ERROR",
                    "errorMessage", ex.getMessage()
            ))));
        } catch (Exception ex) {
            log.error("WebSocket message handling failed", ex);
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"message\":\"internal error\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = userId(session);
        if (userId != null) {
            sessionRegistry.unregister(userId);
        }
    }

    private void handleChatMessage(Long userId, JsonNode root) {
        SendMessageRequest request = new SendMessageRequest();
        request.setMessageType(MessageType.valueOf(root.path("messageType").asText("TEXT")));
        request.setContent(root.path("content").asText(null));
        request.setImageUrl(root.path("imageUrl").asText(null));
        Long sessionId = root.path("sessionId").asLong();
        chatService.sendMessageFromWebSocket(userId, sessionId, request);
    }

    private void handleReadReceipt(Long userId, JsonNode root) {
        Long sessionId = root.path("sessionId").asLong();
        chatService.markSessionReadFromWebSocket(userId, sessionId);
    }

    private Long userId(WebSocketSession session) {
        Object value = session.getAttributes().get(ChatWebSocketAuthInterceptor.USER_ID_ATTR);
        return value instanceof Long id ? id : null;
    }
}
