package com.swapcampus.user.vo;

public class UserResponse {

    private String module;
    private String status;

    public static UserResponse placeholder(String module) {
        UserResponse response = new UserResponse();
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
