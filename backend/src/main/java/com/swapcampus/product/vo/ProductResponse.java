package com.swapcampus.product.vo;

public class ProductResponse {

    private String module;
    private String status;

    public static ProductResponse placeholder(String module) {
        ProductResponse response = new ProductResponse();
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
