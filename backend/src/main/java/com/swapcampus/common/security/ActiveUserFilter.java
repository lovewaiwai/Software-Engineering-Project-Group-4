package com.swapcampus.common.security;

import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.user.service.UserAccountGuard;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActiveUserFilter extends OncePerRequestFilter {

    private final UserAccountGuard userAccountGuard;
    private final SecurityErrorResponseWriter responseWriter;

    public ActiveUserFilter(UserAccountGuard userAccountGuard,
                            SecurityErrorResponseWriter responseWriter) {
        this.userAccountGuard = userAccountGuard;
        this.responseWriter = responseWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                      HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {
        var userId = CurrentUserContext.currentUserId();
        if (userId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (userAccountGuard.isBanned(userId.get())) {
            responseWriter.write(
                    response,
                    HttpStatus.FORBIDDEN.value(),
                    ErrorCode.FORBIDDEN,
                    UserAccountGuard.BANNED_MESSAGE
            );
            return;
        }
        filterChain.doFilter(request, response);
    }
}
