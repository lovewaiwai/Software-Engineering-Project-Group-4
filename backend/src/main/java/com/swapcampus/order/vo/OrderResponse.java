package com.swapcampus.order.vo;

public class OrderResponse {

    private String module;
    private String status;

    public static OrderResponse placeholder(String module) {
        OrderResponse response = new OrderResponse();
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
