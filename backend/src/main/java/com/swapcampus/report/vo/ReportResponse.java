package com.swapcampus.report.vo;

public class ReportResponse {

    private String module;
    private String status;

    public static ReportResponse placeholder(String module) {
        ReportResponse response = new ReportResponse();
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
