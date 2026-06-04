package com.swapcampus.common.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // TODO: Route chat messages through chat service and persist chat_messages.
        session.sendMessage(new TextMessage("{\"type\":\"ACK\",\"status\":\"TODO\"}"));
    }
}
