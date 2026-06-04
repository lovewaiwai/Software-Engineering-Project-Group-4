import { defineStore } from 'pinia'

interface AuthState {
  token: string
  userId: number | null
  role: 'USER' | 'ADMIN' | 'SYS_ADMIN' | ''
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('swapcampus_token') ?? '',
    userId: null,
    role: '',
  }),
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('swapcampus_token', token)
    },
    clearSession() {
      this.token = ''
      this.userId = null
      this.role = ''
      localStorage.removeItem('swapcampus_token')
    },
  },
})
