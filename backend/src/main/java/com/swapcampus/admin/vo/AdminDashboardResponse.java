package com.swapcampus.admin.vo;

import java.math.BigDecimal;

public class AdminDashboardResponse {

    private long pendingReports;
    private long todayReports;
    private long activeChatUsers;

    private long totalUsers;
    private long activeUsers;
    private long bannedUsers;
    private long todayNewUsers;
    private long todayActiveUsers;

    private long totalProducts;
    private long activeProducts;
    private long todayNewProducts;

    private long totalOrders;
    private long completedOrders;
    private long todayNewOrders;
    private BigDecimal totalGmv = BigDecimal.ZERO;
    private BigDecimal todayGmv = BigDecimal.ZERO;

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

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(long bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public long getTodayNewUsers() {
        return todayNewUsers;
    }

    public void setTodayNewUsers(long todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public long getTodayActiveUsers() {
        return todayActiveUsers;
    }

    public void setTodayActiveUsers(long todayActiveUsers) {
        this.todayActiveUsers = todayActiveUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public long getTodayNewProducts() {
        return todayNewProducts;
    }

    public void setTodayNewProducts(long todayNewProducts) {
        this.todayNewProducts = todayNewProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public long getTodayNewOrders() {
        return todayNewOrders;
    }

    public void setTodayNewOrders(long todayNewOrders) {
        this.todayNewOrders = todayNewOrders;
    }

    public BigDecimal getTotalGmv() {
        return totalGmv;
    }

    public void setTotalGmv(BigDecimal totalGmv) {
        this.totalGmv = totalGmv;
    }

    public BigDecimal getTodayGmv() {
        return todayGmv;
    }

    public void setTodayGmv(BigDecimal todayGmv) {
        this.todayGmv = todayGmv;
    }
}
