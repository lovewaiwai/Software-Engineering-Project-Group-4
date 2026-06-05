package com.swapcampus.delivery.dto;

import jakarta.validation.constraints.NotNull;

public class DeliveryRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long stationId;

    private String size = "M";

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}