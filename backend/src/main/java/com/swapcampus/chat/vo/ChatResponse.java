package com.swapcampus.chat.vo;

public class ChatResponse {

    private String module;
    private String status;

    public static ChatResponse placeholder(String module) {
        ChatResponse response = new ChatResponse();
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
