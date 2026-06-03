package com.swapcampus.audit.vo;

public class AuditResponse {

    private String module;
    private String status;

    public static AuditResponse placeholder(String module) {
        AuditResponse response = new AuditResponse();
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
