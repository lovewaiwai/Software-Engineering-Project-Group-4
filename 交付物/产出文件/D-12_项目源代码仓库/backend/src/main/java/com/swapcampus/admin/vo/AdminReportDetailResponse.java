package com.swapcampus.admin.vo;

import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.report.vo.ReportResponse;

import java.util.List;

public class AdminReportDetailResponse {

    private ReportResponse report;
    private List<ChatMessageResponse> contextMessages;
    private AdminUserSummaryResponse reportedUser;

    public ReportResponse getReport() {
        return report;
    }

    public void setReport(ReportResponse report) {
        this.report = report;
    }

    public List<ChatMessageResponse> getContextMessages() {
        return contextMessages;
    }

    public void setContextMessages(List<ChatMessageResponse> contextMessages) {
        this.contextMessages = contextMessages;
    }

    public AdminUserSummaryResponse getReportedUser() {
        return reportedUser;
    }

    public void setReportedUser(AdminUserSummaryResponse reportedUser) {
        this.reportedUser = reportedUser;
    }
}
