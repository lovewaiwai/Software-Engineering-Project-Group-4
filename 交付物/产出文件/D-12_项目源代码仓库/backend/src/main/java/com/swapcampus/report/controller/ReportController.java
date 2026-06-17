package com.swapcampus.report.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.report.dto.CreateReportRequest;
import com.swapcampus.report.service.ReportService;
import com.swapcampus.report.vo.ReportResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ApiResponse<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(reportService.createReport(userId, request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<ReportResponse>> listMyReports() {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(reportService.listMyReports(userId));
    }
}
