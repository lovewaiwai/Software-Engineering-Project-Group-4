package com.swapcampus.audit.service.impl;

import com.swapcampus.audit.service.AuditService;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    @Override
    public String moduleName() {
        return "audit";
    }
}
