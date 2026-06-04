package com.swapcampus.admin.vo;

public class AdminResponse {

    private String module;
    private String status;

    public static AdminResponse placeholder(String module) {
        AdminResponse response = new AdminResponse();
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
