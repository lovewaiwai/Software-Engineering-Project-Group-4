package com.swapcampus.audit.service.impl;

import com.swapcampus.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Override
    public void record(Long operatorId, String action, String targetType, Long targetId, String detail) {
        // TODO: Persist to audit_logs through AuditMapper after audit fields are finalized.
    }
}
