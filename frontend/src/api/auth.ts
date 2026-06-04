import { apiClient } from './client'
import type { ApiResponse, AuthResponse } from './types'

export async function login(username: string, password: string) {
  const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', { username, password })
  return data
}

export async function register(username: string, password: string, email?: string) {
  const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', { username, password, email })
  return data
}
