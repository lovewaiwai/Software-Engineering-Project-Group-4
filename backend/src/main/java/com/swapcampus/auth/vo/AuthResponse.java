package com.swapcampus.auth.vo;

import com.swapcampus.user.vo.UserResponse;

public class AuthResponse {

    private String token;
    private String tokenType;
    private UserResponse user;

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

}
