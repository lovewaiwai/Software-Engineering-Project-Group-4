import { apiClient } from './client'
import type { AdminDashboard, AdminReportDetail, AdminUserSummary, ApiResponse, ReportItem } from './types'

export async function fetchDashboard() {
  const { data } = await apiClient.get<ApiResponse<AdminDashboard>>('/admin/dashboard')
  return data
}

export async function listPendingReports() {
  const { data } = await apiClient.get<ApiResponse<ReportItem[]>>('/admin/reports')
  return data
}

export async function fetchReportDetail(reportId: number) {
  const { data } = await apiClient.get<ApiResponse<AdminReportDetail>>(`/admin/reports/${reportId}`)
  return data
}

export async function handleReport(reportId: number, payload: { actionType: string; note?: string; muteHours?: number }) {
  const { data } = await apiClient.post<ApiResponse<ReportItem>>(`/admin/reports/${reportId}/actions`, payload)
  return data
}

export async function listAdminUsers(keyword?: string) {
  const { data } = await apiClient.get<ApiResponse<AdminUserSummary[]>>('/admin/users', { params: { keyword } })
  return data
}

export async function banUser(userId: number, note?: string) {
  const { data } = await apiClient.post<ApiResponse<AdminUserSummary>>(`/admin/users/${userId}/ban`, { note })
  return data
}

export async function unbanUser(userId: number, note?: string) {
  const { data } = await apiClient.post<ApiResponse<AdminUserSummary>>(`/admin/users/${userId}/unban`, { note })
  return data
}

export async function muteUser(userId: number, payload?: { note?: string; muteHours?: number }) {
  const { data } = await apiClient.post<ApiResponse<AdminUserSummary>>(`/admin/users/${userId}/mute`, payload ?? {})
  return data
}

export async function unmuteUser(userId: number, note?: string) {
  const { data } = await apiClient.post<ApiResponse<AdminUserSummary>>(`/admin/users/${userId}/unmute`, { note })
  return data
}
