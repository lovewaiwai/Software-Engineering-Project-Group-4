import { apiClient } from './client'
import type { ApiResponse, ReportItem } from './types'

export async function createReport(payload: {
  targetType: string
  targetId: number
  sessionId?: number
  reportedUserId?: number
  reason: string
  description?: string
  evidenceUrl?: string
}) {
  const { data } = await apiClient.post<ApiResponse<ReportItem>>('/reports', payload)
  return data
}

export async function listMyReports() {
  const { data } = await apiClient.get<ApiResponse<ReportItem[]>>('/reports/mine')
  return data
}
