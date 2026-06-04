package com.swapcampus.common.config;

import com.swapcampus.chat.websocket.ChatWebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatWebSocketAuthInterceptor chatWebSocketAuthInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           ChatWebSocketAuthInterceptor chatWebSocketAuthInterceptor) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.chatWebSocketAuthInterceptor = chatWebSocketAuthInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(chatWebSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
