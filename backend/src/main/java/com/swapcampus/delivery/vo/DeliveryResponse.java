package com.swapcampus.delivery.vo;

public class DeliveryResponse {

    private String module;
    private String status;

    public static DeliveryResponse placeholder(String module) {
        DeliveryResponse response = new DeliveryResponse();
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
