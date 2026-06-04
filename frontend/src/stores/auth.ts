import { defineStore } from 'pinia'
import type { UserInfo } from '../api/types'
import { login as loginApi, register as registerApi } from '../api/auth'

interface AuthState {
  token: string
  userId: number | null
  username: string
  realName: string
  role: 'USER' | 'ADMIN' | 'SYS_ADMIN' | ''
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('swapcampus_token') ?? '',
    userId: localStorage.getItem('swapcampus_user_id') ? Number(localStorage.getItem('swapcampus_user_id')) : null,
    username: localStorage.getItem('swapcampus_username') ?? '',
    realName: localStorage.getItem('swapcampus_real_name') ?? '',
    role: (localStorage.getItem('swapcampus_role') as AuthState['role']) ?? '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN' || state.role === 'SYS_ADMIN',
    displayName: (state) => state.realName || state.username || '用户',
  },
  actions: {
    applySession(token: string, user: UserInfo) {
      this.token = token
      this.userId = user.id
      this.username = user.username
      this.realName = user.profile?.realName ?? ''
      this.role = user.role as AuthState['role']
      localStorage.setItem('swapcampus_token', token)
      localStorage.setItem('swapcampus_user_id', String(user.id))
      localStorage.setItem('swapcampus_username', user.username)
      localStorage.setItem('swapcampus_real_name', this.realName)
      localStorage.setItem('swapcampus_role', user.role)
    },
    async login(username: string, password: string) {
      const response = await loginApi(username, password)
      if (response.code !== 0) {
        throw new Error(response.message || '登录失败')
      }
      this.applySession(response.data.token, response.data.user)
    },
    async register(username: string, password: string, email?: string) {
      const response = await registerApi(username, password, email)
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
      this.role = ''
      localStorage.removeItem('swapcampus_token')
      localStorage.removeItem('swapcampus_user_id')
      localStorage.removeItem('swapcampus_username')
      localStorage.removeItem('swapcampus_real_name')
      localStorage.removeItem('swapcampus_role')
    },
    updateProfile(profile?: UserInfo['profile']) {
      if (profile?.realName) {
        this.realName = profile.realName
        localStorage.setItem('swapcampus_real_name', profile.realName)
      }
    },
  },
})
