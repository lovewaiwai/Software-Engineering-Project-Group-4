export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface ModuleHealth {
  module: string
  status: string
}

export interface UserInfo {
  id: number
  username: string
  role: string
  profile?: {
    realName?: string
    studentNo?: string
    college?: string
    grade?: string
    verifiedAt?: string
  }
}

export interface AuthResponse {
  token: string
  tokenType: string
  user: UserInfo
}

export type MessageType = 'TEXT' | 'IMAGE' | 'EMOJI'
export type MessageStatus = 'SENT' | 'READ' | 'RECALLED'

export interface ChatMessage {
  id: number
  sessionId: number
  senderId: number
  messageType: MessageType
  content: string
  imageUrl?: string
  seqNo: number
  status: MessageStatus
  readAt?: string
  createdAt: string
}

export interface ChatSession {
  id: number
  productId?: number
  buyerId: number
  sellerId: number
  peerId: number
  peerUsername?: string
  lastPreview?: string
  unreadCount: number
  lastMessageAt?: string
  createdAt: string
}

export interface ReportItem {
  id: number
  reporterId: number
  targetType: string
  targetId: number
  sessionId?: number
  reportedUserId?: number
  reason: string
  description?: string
  evidenceUrl?: string
  status: string
  rejectReason?: string
  createdAt: string
}

export interface AdminDashboard {
  pendingReports: number
  todayReports: number
  activeChatUsers: number
}

export interface AdminReportDetail {
  report: ReportItem
  contextMessages: ChatMessage[]
  reportedUser?: AdminUserSummary
}

export interface AdminUserSummary {
  id: number
  username: string
  realName?: string
  role: string
  status: string
  muted: boolean
  mutedUntil?: string
  creditScore?: number
}
