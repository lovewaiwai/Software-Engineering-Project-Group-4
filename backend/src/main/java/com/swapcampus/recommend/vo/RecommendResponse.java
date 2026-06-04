package com.swapcampus.recommend.vo;

public class RecommendResponse {

    private String module;
    private String status;

    public static RecommendResponse placeholder(String module) {
        RecommendResponse response = new RecommendResponse();
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
