import { apiClient } from './client'
import type { ApiResponse, ChatMessage, ChatSession } from './types'

export async function createSession(payload: { sellerId: number; productId?: number }) {
  const { data } = await apiClient.post<ApiResponse<ChatSession>>('/chat/sessions', payload)
  return data
}

export async function listSessions() {
  const { data } = await apiClient.get<ApiResponse<ChatSession[]>>('/chat/sessions')
  return data
}

export async function listMessages(sessionId: number, limit = 30) {
  const { data } = await apiClient.get<ApiResponse<ChatMessage[]>>(`/chat/sessions/${sessionId}/messages`, {
    params: { limit },
  })
  return data
}

export async function sendMessage(sessionId: number, payload: { messageType: string; content?: string; imageUrl?: string }) {
  const { data } = await apiClient.post<ApiResponse<ChatMessage>>(`/chat/sessions/${sessionId}/messages`, payload)
  return data
}

export async function markSessionRead(sessionId: number) {
  const { data } = await apiClient.post<ApiResponse<ChatMessage[]>>(`/chat/sessions/${sessionId}/read`)
  return data
}

export async function recallMessage(messageId: number) {
  const { data } = await apiClient.post<ApiResponse<ChatMessage>>(`/chat/messages/${messageId}/recall`)
  return data
}

export async function uploadChatImage(file: File) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await apiClient.post<ApiResponse<{ url: string }>>('/chat/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}
