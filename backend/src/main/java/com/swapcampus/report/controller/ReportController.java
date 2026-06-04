package com.swapcampus.report.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.report.service.ReportService;
import com.swapcampus.report.vo.ReportResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/health")
    public ApiResponse<ReportResponse> health() {
        return ApiResponse.ok(ReportResponse.placeholder(reportService.moduleName()));
    }
}
