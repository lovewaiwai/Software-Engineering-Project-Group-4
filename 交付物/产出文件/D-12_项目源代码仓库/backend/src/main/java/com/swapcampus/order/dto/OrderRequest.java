package com.swapcampus.order.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequest {

    @NotNull
    private Long productId;

    @NotNull
    private String tradeMode; // MEETUP 或 LOCKER

    private Long lockerStationId; // tradeMode=LOCKER 时必填

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getTradeMode() { return tradeMode; }
    public void setTradeMode(String tradeMode) { this.tradeMode = tradeMode; }
    public Long getLockerStationId() { return lockerStationId; }
    public void setLockerStationId(Long lockerStationId) { this.lockerStationId = lockerStationId; }
}