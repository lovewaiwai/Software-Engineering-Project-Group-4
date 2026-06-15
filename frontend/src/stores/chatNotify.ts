import { defineStore } from 'pinia'
import type { ChatSession } from '../api/types'
import { loadSessions, subscribeChatSocket, type WsPayload } from './chat'
import { sumUnreadCount } from '../utils/unread'
import { useAuthStore } from './auth'

export const useChatNotifyStore = defineStore('chatNotify', {
  state: () => ({
    totalUnread: 0,
    sessions: [] as ChatSession[],
    loading: false,
    pollTimer: null as ReturnType<typeof setInterval> | null,
    unsubscribeWs: null as (() => void) | null,
  }),
  getters: {
    hasUnread: (state) => state.totalUnread > 0,
  },
  actions: {
    applySessions(sessions: ChatSession[]) {
      this.sessions = sessions
      this.totalUnread = sumUnreadCount(sessions)
    },
    async refresh() {
      const auth = useAuthStore()
      if (!auth.isLoggedIn || auth.isAdmin) {
        this.applySessions([])
        return
      }
      this.loading = true
      try {
        const sessions = await loadSessions()
        this.applySessions(sessions)
      } catch {
        // keep previous counts when refresh fails
      } finally {
        this.loading = false
      }
    },
    start() {
      this.stop()
      void this.refresh()
      this.pollTimer = setInterval(() => {
        void this.refresh()
      }, 30000)
      this.unsubscribeWs = subscribeChatSocket((payload: WsPayload) => {
        if (payload.type === 'CHAT_MESSAGE' || payload.type === 'READ_RECEIPT') {
          void this.refresh()
        }
      })
    },
    stop() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
      if (this.unsubscribeWs) {
        this.unsubscribeWs()
        this.unsubscribeWs = null
      }
      this.applySessions([])
    },
  },
})
