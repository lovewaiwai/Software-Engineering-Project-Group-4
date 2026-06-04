package com.swapcampus.payment.vo;

public class PaymentResponse {

    private String module;
    private String status;

    public static PaymentResponse placeholder(String module) {
        PaymentResponse response = new PaymentResponse();
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
