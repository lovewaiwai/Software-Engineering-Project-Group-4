import { defineStore } from 'pinia'
import type { UserInfo } from '../api/types'
import { login as loginApi, register as registerApi } from '../api/auth'

interface AuthState {
  token: string
  userId: number | null
  username: string
  realName: string
  verifiedAt: string
  role: 'USER' | 'PRODUCT_REVIEWER' | 'ADMIN' | 'SYS_ADMIN' | ''
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('swapcampus_token') ?? '',
    userId: localStorage.getItem('swapcampus_user_id') ? Number(localStorage.getItem('swapcampus_user_id')) : null,
    username: localStorage.getItem('swapcampus_username') ?? '',
    realName: localStorage.getItem('swapcampus_real_name') ?? '',
    verifiedAt: localStorage.getItem('swapcampus_verified_at') ?? '',
    role: (localStorage.getItem('swapcampus_role') as AuthState['role']) ?? '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isVerified: (state) => !!state.verifiedAt,
    isAdmin: (state) => state.role === 'PRODUCT_REVIEWER' || state.role === 'ADMIN' || state.role === 'SYS_ADMIN',
    isSystemReviewer: (state) => state.role === 'ADMIN' || state.role === 'SYS_ADMIN',
    canReviewProducts: (state) => state.role === 'PRODUCT_REVIEWER' || state.role === 'ADMIN' || state.role === 'SYS_ADMIN',
    displayName: (state) => state.realName || state.username || '用户',
  },
  actions: {
    applySession(token: string, user: UserInfo) {
      this.token = token
      this.userId = user.id
      this.username = user.username
      this.realName = user.profile?.realName ?? ''
      this.verifiedAt = user.profile?.verifiedAt ?? ''
      this.role = user.role as AuthState['role']
      localStorage.setItem('swapcampus_token', token)
      localStorage.setItem('swapcampus_user_id', String(user.id))
      localStorage.setItem('swapcampus_username', user.username)
      localStorage.setItem('swapcampus_real_name', this.realName)
      localStorage.setItem('swapcampus_verified_at', this.verifiedAt)
      localStorage.setItem('swapcampus_role', user.role)
    },
    async login(username: string, password: string) {
      const response = await loginApi(username, password)
      if (response.code !== 0) {
        throw new Error(response.message || '登录失败')
      }
      this.applySession(response.data.token, response.data.user)
    },
    async register(username: string, password: string, phone?: string, email?: string) {
      const response = await registerApi(username, password, phone, email)
      if (response.code !== 0) {
        throw new Error(response.message || '注册失败')
      }
      this.applySession(response.data.token, response.data.user)
    },
    clearSession() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.verifiedAt = ''
      this.role = ''
      localStorage.removeItem('swapcampus_token')
      localStorage.removeItem('swapcampus_user_id')
      localStorage.removeItem('swapcampus_username')
      localStorage.removeItem('swapcampus_real_name')
      localStorage.removeItem('swapcampus_verified_at')
      localStorage.removeItem('swapcampus_role')
    },
    updateProfile(profile?: UserInfo['profile']) {
      if (profile?.realName) {
        this.realName = profile.realName
        localStorage.setItem('swapcampus_real_name', profile.realName)
      }
      this.verifiedAt = profile?.verifiedAt ?? ''
      if (this.verifiedAt) {
        localStorage.setItem('swapcampus_verified_at', this.verifiedAt)
      } else {
        localStorage.removeItem('swapcampus_verified_at')
      }
    },
  },
})
