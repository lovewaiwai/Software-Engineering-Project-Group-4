import type { ChatMessage, ChatSession } from '../api/types'
import { createSession, listMessages, listSessions, markSessionRead, sendMessage, uploadChatImage } from '../api/chat'
import { createReport } from '../api/report'
import { useAuthStore } from './auth'

type WsPayload = {
  type: string
  message?: ChatMessage
  messages?: ChatMessage[]
  sessionId?: number
  errorMessage?: string
}

let socket: WebSocket | null = null
const listeners = new Set<(payload: WsPayload) => void>()

function resolveWsUrl(token: string): string {
  const configured = import.meta.env.VITE_WS_BASE_URL as string | undefined
  if (configured) {
    const base = configured.replace(/\/$/, '')
    return `${base}/ws/chat?token=${encodeURIComponent(token)}`
  }
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined
  if (apiBaseUrl?.startsWith('http')) {
    const apiUrl = new URL(apiBaseUrl)
    apiUrl.protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
    apiUrl.pathname = '/ws/chat'
    apiUrl.search = `token=${encodeURIComponent(token)}`
    return apiUrl.toString()
  }
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${protocol}://${window.location.host}/ws/chat?token=${encodeURIComponent(token)}`
}

function dispatchWsPayload(payload: WsPayload) {
  listeners.forEach((listener) => listener(payload))
}

function ensureChatSocket() {
  const auth = useAuthStore()
  if (!auth.token || listeners.size === 0) return
  if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }
  socket?.close()
  socket = new WebSocket(resolveWsUrl(auth.token))
  socket.onmessage = (event) => {
    try {
      dispatchWsPayload(JSON.parse(event.data))
    } catch {
      // ignore malformed payloads
    }
  }
  socket.onclose = () => {
    if (listeners.size > 0) {
      window.setTimeout(() => ensureChatSocket(), 3000)
    }
  }
}

export function subscribeChatSocket(onEvent: (payload: WsPayload) => void) {
  listeners.add(onEvent)
  ensureChatSocket()
  return () => {
    listeners.delete(onEvent)
    if (listeners.size === 0) {
      socket?.close()
      socket = null
    }
  }
}

/** @deprecated use subscribeChatSocket */
export function connectChatSocket(onEvent: (payload: WsPayload) => void) {
  subscribeChatSocket(onEvent)
}

/** @deprecated listeners manage socket lifecycle */
export function disconnectChatSocket() {
  listeners.clear()
  socket?.close()
  socket = null
}

export function sendWsMessage(payload: Record<string, unknown>) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify(payload))
  }
}

export async function openOrCreateSession(sellerId: number, productId?: number) {
  const response = await createSession({ sellerId, productId })
  if (response.code !== 0) throw new Error(response.message)
  return response.data
}

export async function loadSessions() {
  const response = await listSessions()
  if (response.code !== 0) throw new Error(response.message)
  return response.data
}

export async function loadMessages(sessionId: number) {
  const response = await listMessages(sessionId)
  if (response.code !== 0) throw new Error(response.message)
  return response.data.sort((a, b) => a.seqNo - b.seqNo)
}

export async function readSession(sessionId: number) {
  const response = await markSessionRead(sessionId)
  if (response.code !== 0) throw new Error(response.message)
  return response.data
}

export async function sendChatMessage(
  sessionId: number,
  messageType: 'TEXT' | 'IMAGE' | 'EMOJI',
  content?: string,
  imageUrl?: string,
) {
  sendWsMessage({ type: 'CHAT_MESSAGE', sessionId, messageType, content, imageUrl })
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    const response = await sendMessage(sessionId, { messageType, content, imageUrl })
    if (response.code !== 0) throw new Error(response.message)
    return response.data
  }
  return null
}

export async function uploadImage(file: File) {
  const response = await uploadChatImage(file)
  if (response.code !== 0) throw new Error(response.message)
  return response.data.url
}

export async function submitChatReport(payload: {
  targetType: 'CHAT_MESSAGE' | 'USER'
  targetId: number
  sessionId?: number
  reportedUserId?: number
  reason: string
  description?: string
  evidenceUrl?: string
}) {
  const response = await createReport(payload)
  if (response.code !== 0) throw new Error(response.message || '举报提交失败')
  return response.data
}

/** @deprecated use submitChatReport */
export async function submitMessageReport(message: ChatMessage, reason: string) {
  return submitChatReport({
    targetType: 'CHAT_MESSAGE',
    targetId: message.id,
    sessionId: message.sessionId,
    reportedUserId: message.senderId,
    reason,
    description: message.content,
  })
}

export type { ChatMessage, ChatSession, WsPayload }
