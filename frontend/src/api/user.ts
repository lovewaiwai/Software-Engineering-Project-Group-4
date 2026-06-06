import { apiClient } from './client'
import type { ApiResponse, CreditRecord, PageResponse, UserInfo } from './types'

export interface UserProfileUpdatePayload {
  phone?: string
  email?: string
  bio?: string
  avatarUrl?: string
}

export async function verifyStudent(payload: {
  studentNo: string
  realName: string
  college: string
  grade: string
}) {
  const { data } = await apiClient.post<ApiResponse<UserInfo>>('/users/me/verify-student', payload)
  return data
}

export async function fetchCurrentUser() {
  const { data } = await apiClient.get<ApiResponse<UserInfo>>('/users/me')
  return data
}

export async function fetchUserById(id: number | string) {
  const { data } = await apiClient.get<ApiResponse<UserInfo>>(`/users/${id}`)
  return data
}

export async function updateCurrentUserProfile(payload: UserProfileUpdatePayload) {
  const { data } = await apiClient.put<ApiResponse<UserInfo>>('/users/me/profile', payload)
  return data
}

export async function fetchCreditRecords(page = 1, pageSize = 20) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<CreditRecord>>>('/users/me/credit-records', {
    params: { page, pageSize },
  })
  return data
}
