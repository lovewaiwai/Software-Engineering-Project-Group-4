/** 将后端返回的相对路径转为浏览器可访问的完整 URL */
export function resolveMediaUrl(url?: string | null): string {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:')) {
    return url
  }
  const normalized = url.startsWith('/') ? url : `/${url}`
  const backendOrigin = (import.meta.env.VITE_BACKEND_ORIGIN as string | undefined) ?? ''
  if (backendOrigin) {
    return `${backendOrigin.replace(/\/$/, '')}${normalized}`
  }
  return normalized
}

export function formatMessageTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function peerInitial(name?: string): string {
  if (!name) return '?'
  return name.trim().charAt(0).toUpperCase()
}
