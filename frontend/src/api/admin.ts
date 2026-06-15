import { apiClient } from './client'
import type { AdminDashboard, AdminReportDetail, AdminUserSummary, ApiResponse, ReportItem } from './types'
import type { ProductItem } from './product'

export async function fetchDashboard() {
  const { data } = await apiClient.get<ApiResponse<AdminDashboard>>('/admin/dashboard')
  return data
}

export async function listPendingProducts() {
  const { data } = await apiClient.get<ApiResponse<ProductItem[]>>('/admin/products/pending')
  return data
}

export async function approveProduct(productId: number) {
  const { data } = await apiClient.post<ApiResponse<ProductItem>>(`/admin/products/${productId}/approve`)
  return data
}

export async function bulkApproveProducts(keywords: string[]) {
  const { data } = await apiClient.post<ApiResponse<ProductItem[]>>('/admin/products/bulk-approve', { keywords })
  return data
}

export async function rejectProduct(productId: number) {
  const { data } = await apiClient.post<ApiResponse<ProductItem>>(`/admin/products/${productId}/reject`)
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

export interface LockerStation {
  id: number
  name: string
  location: string
  status: string
  emptyBoxes: number
  reservedBoxes: number
  occupiedBoxes: number
}

export interface LockerTask {
  id: number
  taskNo: string
  orderId: number
  stationName: string
  boxNo: string
  status: string
  pickupCode: string
  storedAt?: string
  pickedUpAt?: string
  createdAt: string
}

export async function listLockerStations() {
  const { data } = await apiClient.get<ApiResponse<LockerStation[]>>('/admin/lockers/stations')
  return data
}

export async function listLockerTasks() {
  const { data } = await apiClient.get<ApiResponse<LockerTask[]>>('/admin/lockers/tasks')
  return data
}
