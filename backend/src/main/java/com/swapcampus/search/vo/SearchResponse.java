package com.swapcampus.search.vo;

public class SearchResponse {

    private String module;
    private String status;

    public static SearchResponse placeholder(String module) {
        SearchResponse response = new SearchResponse();
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
