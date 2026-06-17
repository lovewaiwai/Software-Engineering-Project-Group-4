package com.swapcampus.report.vo;

import com.swapcampus.common.enums.ReportStatus;
import com.swapcampus.common.enums.ReportTargetType;
import com.swapcampus.report.entity.ReportEntity;

import java.time.LocalDateTime;

public class ReportResponse {

    private Long id;
    private Long reporterId;
    private ReportTargetType targetType;
    private Long targetId;
    private Long sessionId;
    private Long reportedUserId;
    private String reason;
    private String description;
    private String evidenceUrl;
    private ReportStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static ReportResponse from(ReportEntity entity) {
        ReportResponse response = new ReportResponse();
        response.setId(entity.getId());
        response.setReporterId(entity.getReporterId());
        response.setTargetType(entity.getTargetType());
        response.setTargetId(entity.getTargetId());
        response.setSessionId(entity.getSessionId());
        response.setReportedUserId(entity.getReportedUserId());
        response.setReason(entity.getReason());
        response.setDescription(entity.getDescription());
        response.setEvidenceUrl(entity.getEvidenceUrl());
        response.setStatus(entity.getStatus());
        response.setRejectReason(entity.getRejectReason());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
