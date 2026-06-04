package com.swapcampus.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int httpStatus, ErrorCode errorCode) throws IOException {
        write(response, httpStatus, errorCode, errorCode.getMessage());
    }

    public void write(HttpServletResponse response, int httpStatus, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(errorCode.getCode(), message));
    }
}
