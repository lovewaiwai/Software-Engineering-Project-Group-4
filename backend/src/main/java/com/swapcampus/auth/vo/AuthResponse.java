package com.swapcampus.auth.vo;

import com.swapcampus.user.vo.UserResponse;

public class AuthResponse {

    private String token;
    private String tokenType;
    private UserResponse user;
    private String module;
    private String status;

    public static AuthResponse placeholder(String module) {
        AuthResponse response = new AuthResponse();
        response.setModule(module);
        response.setStatus("TODO");
        return response;
    }

    public static AuthResponse login(String token, UserResponse user) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setUser(user);
        return response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
