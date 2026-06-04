package com.swapcampus.report.service;

import com.swapcampus.report.dto.CreateReportRequest;
import com.swapcampus.report.vo.ReportResponse;

import java.util.List;

public interface ReportService {

    String moduleName();

    ReportResponse createReport(Long reporterId, CreateReportRequest request);

    List<ReportResponse> listMyReports(Long reporterId);
}
