package com.swapcampus.audit.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.audit.service.AuditService;
import com.swapcampus.audit.vo.AuditResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/health")
    public ApiResponse<AuditResponse> health() {
        return ApiResponse.ok(AuditResponse.placeholder(auditService.moduleName()));
    }
}
