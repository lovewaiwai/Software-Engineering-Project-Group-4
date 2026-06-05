export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  items: T[]
  page: number
  pageSize: number
  total: number
}

export interface ModuleHealth {
  module: string
  status: string
}

export interface UserProfile {
  realName?: string
  studentNo?: string
  college?: string
  grade?: string
  avatarUrl?: string
  bio?: string
  verifiedAt?: string
  contactMasked?: string
}

export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  role: string
  status?: string
  creditScore?: number
  pointBalance?: number
  createdAt?: string
  profile?: UserProfile
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

export interface CreditRecord {
  id: number
  delta: number
  scoreAfter: number
  reason: string
  refType?: string
  refId?: number
  createdAt: string
}

export interface PointTask {
  id: number
  code: string
  name: string
  rewardPoints: number
  taskType: string
  status: string
  claimed: boolean
  claimable: boolean
}

export interface PointRecord {
  id: number
  delta: number
  balanceAfter: number
  reason: string
  refType?: string
  refId?: number
  createdAt: string
}

export interface PointItem {
  itemCode: string
  itemName: string
  costPoints: number
  description?: string
}

export interface PointRedemption {
  id: number
  itemCode: string
  itemName: string
  costPoints: number
  status: string
  balanceAfter: number
  createdAt: string
}
