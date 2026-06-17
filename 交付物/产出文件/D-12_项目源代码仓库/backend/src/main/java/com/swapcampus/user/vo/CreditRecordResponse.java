package com.swapcampus.user.vo;

import com.swapcampus.user.entity.CreditRecordEntity;

import java.time.LocalDateTime;

public class CreditRecordResponse {

    private Long id;
    private Integer delta;
    private Integer scoreAfter;
    private String reason;
    private String refType;
    private Long refId;
    private LocalDateTime createdAt;

    public static CreditRecordResponse from(CreditRecordEntity entity) {
        CreditRecordResponse response = new CreditRecordResponse();
        response.setId(entity.getId());
        response.setDelta(entity.getDelta());
        response.setScoreAfter(entity.getScoreAfter());
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

    public Integer getScoreAfter() {
        return scoreAfter;
    }

    public void setScoreAfter(Integer scoreAfter) {
        this.scoreAfter = scoreAfter;
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