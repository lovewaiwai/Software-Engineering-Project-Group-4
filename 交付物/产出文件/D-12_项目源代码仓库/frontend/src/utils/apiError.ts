import axios from 'axios'

export function getApiErrorMessage(error: unknown, fallback = '操作失败'): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message
    if (typeof message === 'string' && message.trim()) {
      return message
    }
  }
  if (error instanceof Error && error.message && !error.message.startsWith('Request failed')) {
    return error.message
  }
  return fallback
}
