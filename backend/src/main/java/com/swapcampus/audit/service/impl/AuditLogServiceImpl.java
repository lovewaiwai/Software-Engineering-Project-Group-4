package com.swapcampus.audit.service.impl;

import com.swapcampus.audit.entity.AuditEntity;
import com.swapcampus.audit.mapper.AuditMapper;
import com.swapcampus.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditMapper auditMapper;

    public AuditLogServiceImpl(AuditMapper auditMapper) {
        this.auditMapper = auditMapper;
    }

    @Override
    public void record(Long operatorId, String action, String targetType, Long targetId, String detail) {
        AuditEntity audit = new AuditEntity();
        audit.setOperatorId(operatorId);
        audit.setAction(action);
        audit.setTargetType(targetType);
        audit.setTargetId(targetId);
        audit.setDetail(detail);
        auditMapper.insert(audit);
    }
}
