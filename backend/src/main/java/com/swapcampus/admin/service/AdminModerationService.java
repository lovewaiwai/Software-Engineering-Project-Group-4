package com.swapcampus.admin.service;

import com.swapcampus.admin.dto.HandleReportRequest;
import com.swapcampus.admin.dto.ProductBulkApproveRequest;
import com.swapcampus.admin.dto.ProductReviewRequest;
import com.swapcampus.admin.vo.AdminDashboardResponse;
import com.swapcampus.admin.vo.AdminReportDetailResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.report.vo.ReportResponse;

import java.util.List;

public interface AdminModerationService {

    AdminDashboardResponse dashboard();

    List<ProductResponse> listPendingProducts();

    ProductResponse approveProduct(Long reviewerId, Long productId);

    List<ProductResponse> bulkApproveProducts(Long reviewerId, ProductBulkApproveRequest request);

    ProductResponse rejectProduct(Long reviewerId, Long productId, ProductReviewRequest request);

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
