package com.swapcampus.chat.websocket;

import com.swapcampus.common.security.JwtTokenProvider;
import com.swapcampus.user.service.UserAccountGuard;
import io.jsonwebtoken.Claims;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class ChatWebSocketAuthInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "userId";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserAccountGuard userAccountGuard;

    public ChatWebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider,
                                          UserAccountGuard userAccountGuard) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userAccountGuard = userAccountGuard;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = extractToken(request.getURI());
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            if (userAccountGuard.isBanned(userId)) {
                return false;
            }
            attributes.put(USER_ID_ATTR, userId);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private String extractToken(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        for (String part : uri.getQuery().split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
