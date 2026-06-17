package com.swapcampus.delivery.vo;

import java.time.LocalDateTime;

public class DeliveryResponse {

    private Long id;
    private String taskNo;
    private Long orderId;
    private Long stationId;
    private Long boxId;
    private String stationName;
    private String boxNo;
    private String pickupCode;
    private String status;
    private LocalDateTime storedAt;
    private LocalDateTime pickedUpAt;

    public static DeliveryResponse from(com.swapcampus.delivery.entity.DeliveryEntity e) {
        DeliveryResponse r = new DeliveryResponse();
        r.setId(e.getId());
        r.setTaskNo(e.getTaskNo());
        r.setOrderId(e.getOrderId());
        r.setStationId(e.getStationId());
        r.setBoxId(e.getBoxId());
        r.setPickupCode(e.getPickupCode());
        r.setStatus(e.getStatus());
        r.setStoredAt(e.getStoredAt());
        r.setPickedUpAt(e.getPickedUpAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    public Long getBoxId() { return boxId; }
    public void setBoxId(Long boxId) { this.boxId = boxId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStoredAt() { return storedAt; }
    public void setStoredAt(LocalDateTime storedAt) { this.storedAt = storedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
}
