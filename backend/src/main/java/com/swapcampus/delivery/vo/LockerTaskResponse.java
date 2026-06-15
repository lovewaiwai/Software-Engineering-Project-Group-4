package com.swapcampus.delivery.vo;

import com.swapcampus.delivery.entity.DeliveryEntity;

import java.time.LocalDateTime;

public class LockerTaskResponse {

    private Long id;
    private String taskNo;
    private Long orderId;
    private String stationName;
    private String boxNo;
    private String status;
    private String pickupCode;
    private LocalDateTime storedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime createdAt;

    public static LockerTaskResponse from(DeliveryEntity task) {
        LockerTaskResponse response = new LockerTaskResponse();
        response.setId(task.getId());
        response.setTaskNo(task.getTaskNo());
        response.setOrderId(task.getOrderId());
        response.setStatus(task.getStatus());
        response.setPickupCode(task.getPickupCode());
        response.setStoredAt(task.getStoredAt());
        response.setPickedUpAt(task.getPickedUpAt());
        response.setCreatedAt(task.getCreatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }
    public LocalDateTime getStoredAt() { return storedAt; }
    public void setStoredAt(LocalDateTime storedAt) { this.storedAt = storedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
