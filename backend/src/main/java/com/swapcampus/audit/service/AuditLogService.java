package com.swapcampus.audit.service;

public interface AuditLogService {

    void record(Long operatorId, String action, String targetType, Long targetId, String detail);
}
