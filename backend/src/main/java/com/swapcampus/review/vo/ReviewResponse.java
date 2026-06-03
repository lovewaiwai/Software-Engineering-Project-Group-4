package com.swapcampus.review.vo;

public class ReviewResponse {

    private String module;
    private String status;

    public static ReviewResponse placeholder(String module) {
        ReviewResponse response = new ReviewResponse();
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
