package com.swapcampus.admin.service;

import com.swapcampus.admin.dto.HandleReportRequest;
import com.swapcampus.admin.vo.AdminDashboardResponse;
import com.swapcampus.admin.vo.AdminReportDetailResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.report.vo.ReportResponse;

import java.util.List;

public interface AdminModerationService {

    AdminDashboardResponse dashboard();

    List<ReportResponse> listPendingReports();

    AdminReportDetailResponse getReportDetail(Long reportId);

    ReportResponse handleReport(Long adminId, Long reportId, HandleReportRequest request);

    List<AdminUserSummaryResponse> listUsers(String keyword);

    AdminUserSummaryResponse getUserSummary(Long userId);

    AdminUserSummaryResponse banUser(Long adminId, Long userId, String note);

    AdminUserSummaryResponse unbanUser(Long adminId, Long userId, String note);

    AdminUserSummaryResponse muteUser(Long adminId, Long userId, int hours, String note);

    AdminUserSummaryResponse unmuteUser(Long adminId, Long userId, String note);
}
