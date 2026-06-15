package com.swapcampus.admin.controller;

import com.swapcampus.admin.dto.AdminUserActionRequest;
import com.swapcampus.admin.dto.HandleReportRequest;
import com.swapcampus.admin.dto.ProductBulkApproveRequest;
import com.swapcampus.admin.dto.ProductReviewRequest;
import com.swapcampus.admin.service.AdminModerationService;
import com.swapcampus.admin.vo.AdminDashboardResponse;
import com.swapcampus.admin.vo.AdminReportDetailResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.report.vo.ReportResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    public AdminModerationController(AdminModerationService adminModerationService) {
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/products/pending")
    public ApiResponse<List<ProductResponse>> listPendingProducts() {
        requireProductReviewer();
        return ApiResponse.ok(adminModerationService.listPendingProducts());
    }

    @PostMapping("/products/{productId}/approve")
    public ApiResponse<ProductResponse> approveProduct(@PathVariable Long productId) {
        Long reviewerId = requireProductReviewer();
        return ApiResponse.ok(adminModerationService.approveProduct(reviewerId, productId));
    }

    @PostMapping("/products/bulk-approve")
    public ApiResponse<List<ProductResponse>> bulkApproveProducts(@Valid @RequestBody ProductBulkApproveRequest request) {
        Long reviewerId = requireProductReviewer();
        return ApiResponse.ok(adminModerationService.bulkApproveProducts(reviewerId, request));
    }

    @PostMapping("/products/{productId}/reject")
    public ApiResponse<ProductResponse> rejectProduct(@PathVariable Long productId,
                                                      @Valid @RequestBody(required = false) ProductReviewRequest request) {
        Long reviewerId = requireProductReviewer();
        return ApiResponse.ok(adminModerationService.rejectProduct(reviewerId, productId, request));
    }

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> dashboard() {
        requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.dashboard());
    }

    @GetMapping("/reports")
    public ApiResponse<List<ReportResponse>> listReports() {
        requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.listPendingReports());
    }

    @GetMapping("/reports/{reportId}")
    public ApiResponse<AdminReportDetailResponse> reportDetail(@PathVariable Long reportId) {
        requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.getReportDetail(reportId));
    }

    @PostMapping("/reports/{reportId}/actions")
    public ApiResponse<ReportResponse> handleReport(@PathVariable Long reportId,
                                                    @Valid @RequestBody HandleReportRequest request) {
        Long adminId = requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.handleReport(adminId, reportId, request));
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserSummaryResponse>> listUsers(@RequestParam(required = false) String keyword) {
        requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.listUsers(keyword));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<AdminUserSummaryResponse> userDetail(@PathVariable Long userId) {
        requireSystemReviewer();
        return ApiResponse.ok(adminModerationService.getUserSummary(userId));
    }

    @PostMapping("/users/{userId}/ban")
    public ApiResponse<AdminUserSummaryResponse> banUser(@PathVariable Long userId,
                                                         @RequestBody(required = false) AdminUserActionRequest request) {
        Long adminId = requireSystemReviewer();
        String note = request == null ? null : request.getNote();
        return ApiResponse.ok(adminModerationService.banUser(adminId, userId, note));
    }

    @PostMapping("/users/{userId}/unban")
    public ApiResponse<AdminUserSummaryResponse> unbanUser(@PathVariable Long userId,
                                                           @RequestBody(required = false) AdminUserActionRequest request) {
        Long adminId = requireSystemReviewer();
        String note = request == null ? null : request.getNote();
        return ApiResponse.ok(adminModerationService.unbanUser(adminId, userId, note));
    }

    @PostMapping("/users/{userId}/mute")
    public ApiResponse<AdminUserSummaryResponse> muteUser(@PathVariable Long userId,
                                                          @RequestBody(required = false) AdminUserActionRequest request) {
        Long adminId = requireSystemReviewer();
        int hours = request == null || request.getMuteHours() == null ? 24 : request.getMuteHours();
        String note = request == null ? null : request.getNote();
        return ApiResponse.ok(adminModerationService.muteUser(adminId, userId, hours, note));
    }

    @PostMapping("/users/{userId}/unmute")
    public ApiResponse<AdminUserSummaryResponse> unmuteUser(@PathVariable Long userId,
                                                            @RequestBody(required = false) AdminUserActionRequest request) {
        Long adminId = requireSystemReviewer();
        String note = request == null ? null : request.getNote();
        return ApiResponse.ok(adminModerationService.unmuteUser(adminId, userId, note));
    }

    private Long requireSystemReviewer() {
        Role role = CurrentUserContext.currentRole()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        if (role != Role.SYS_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "需要系统管理员权限");
        }
        return CurrentUserContext.requireUserId();
    }

    private Long requireProductReviewer() {
        Role role = CurrentUserContext.currentRole()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        if (role != Role.PRODUCT_REVIEWER && role != Role.SYS_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "需要商品审核员权限");
        }
        return CurrentUserContext.requireUserId();
    }
}
