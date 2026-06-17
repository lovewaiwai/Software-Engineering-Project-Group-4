package com.swapcampus.payment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private String paymentNo;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String providerTradeNo;
    private String payUrl;
    private LocalDateTime paidAt;

    public static PaymentResponse from(com.swapcampus.payment.entity.PaymentEntity e) {
        PaymentResponse r = new PaymentResponse();
        r.setId(e.getId());
        r.setPaymentNo(e.getPaymentNo());
        r.setOrderId(e.getOrderId());
        r.setAmount(e.getAmount());
        r.setStatus(e.getStatus());
        r.setProviderTradeNo(e.getProviderTradeNo());
        r.setPayUrl(e.getPayUrl());
        r.setPaidAt(e.getPaidAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProviderTradeNo() { return providerTradeNo; }
    public void setProviderTradeNo(String providerTradeNo) { this.providerTradeNo = providerTradeNo; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}