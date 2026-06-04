import { apiClient } from './client'
import type { ApiResponse, UserInfo } from './types'

export async function verifyStudent(payload: {
  studentNo: string
  realName: string
  college: string
  grade?: string
}) {
  const { data } = await apiClient.post<ApiResponse<UserInfo>>('/users/me/verify-student', payload)
  return data
}

export async function fetchCurrentUser() {
  const { data } = await apiClient.get<ApiResponse<UserInfo>>('/users/me')
  return data
}
