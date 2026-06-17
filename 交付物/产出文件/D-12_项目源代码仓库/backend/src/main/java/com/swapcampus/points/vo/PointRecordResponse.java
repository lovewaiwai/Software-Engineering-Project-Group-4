package com.swapcampus.points.vo;

import com.swapcampus.points.entity.PointRecordEntity;

import java.time.LocalDateTime;

public class PointRecordResponse {

    private Long id;
    private Integer delta;
    private Integer balanceAfter;
    private String reason;
    private String refType;
    private Long refId;
    private LocalDateTime createdAt;

    public static PointRecordResponse from(PointRecordEntity entity) {
        PointRecordResponse response = new PointRecordResponse();
        response.setId(entity.getId());
        response.setDelta(entity.getDelta());
        response.setBalanceAfter(entity.getBalanceAfter());
        response.setReason(entity.getReason());
        response.setRefType(entity.getRefType());
        response.setRefId(entity.getRefId());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}