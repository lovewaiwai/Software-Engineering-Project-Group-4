package com.swapcampus.chat.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketSessionRegistry {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    public void unregister(Long userId) {
        userSessions.remove(userId);
    }

    public void disconnect(Long userId) {
        WebSocketSession session = userSessions.remove(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("账号已被封禁"));
        } catch (IOException ignored) {
            // ignore close failure
        }
    }

    public void sendToUser(Long userId, String payload) {
        WebSocketSession session = userSessions.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(payload));
            }
        } catch (IOException ignored) {
            userSessions.remove(userId);
        }
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
}
