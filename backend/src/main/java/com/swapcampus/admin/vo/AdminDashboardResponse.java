package com.swapcampus.admin.vo;

public class AdminDashboardResponse {

    private long pendingReports;
    private long todayReports;
    private long activeChatUsers;

    public long getPendingReports() {
        return pendingReports;
    }

    public void setPendingReports(long pendingReports) {
        this.pendingReports = pendingReports;
    }

    public long getTodayReports() {
        return todayReports;
    }

    public void setTodayReports(long todayReports) {
        this.todayReports = todayReports;
    }

    public long getActiveChatUsers() {
        return activeChatUsers;
    }

    public void setActiveChatUsers(long activeChatUsers) {
        this.activeChatUsers = activeChatUsers;
    }
}
