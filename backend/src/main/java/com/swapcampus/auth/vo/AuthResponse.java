package com.swapcampus.auth.vo;

public class AuthResponse {

    private String module;
    private String status;

    public static AuthResponse placeholder(String module) {
        AuthResponse response = new AuthResponse();
        response.setModule(module);
        response.setStatus("TODO");
        return response;
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
