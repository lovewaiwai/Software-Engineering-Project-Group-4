import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
})

function isPublicAuthRequest(url?: string): boolean {
  if (!url) return false
  return url.includes('/auth/login') || url.includes('/auth/register')
}

apiClient.interceptors.request.use((config) => {
  if (isPublicAuthRequest(config.url)) {
    return config
  }
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message as string | undefined
    const authStore = useAuthStore()
    const onLoginPage = window.location.pathname.startsWith('/login')
    const onVerifyPage = window.location.pathname.startsWith('/verify')
    const authRequest = isPublicAuthRequest(error.config?.url)

    if (status === 403 && message?.includes('学生认证') && authStore.isLoggedIn && !onVerifyPage) {
      const redirect = encodeURIComponent(window.location.pathname + window.location.search)
      window.location.href = `/verify?redirect=${redirect}`
    }

    if (status === 403 && (message?.includes('封禁') || authRequest)) {
      authStore.clearSession()
      if (!onLoginPage && !authRequest) {
        window.location.href = '/login?banned=1'
      }
    }
    return Promise.reject(error)
  },
)
