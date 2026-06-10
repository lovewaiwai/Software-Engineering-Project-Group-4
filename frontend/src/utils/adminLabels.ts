export function formatDateTime(value?: string | null): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function userStatusLabel(status?: string): string {
  if (status === 'BANNED') return '已封禁'
  if (status === 'ACTIVE') return '正常'
  return status || '-'
}

export function userStatusTagType(status?: string, muted?: boolean): 'success' | 'warning' | 'danger' | 'info' {
  if (status === 'BANNED') return 'danger'
  if (muted) return 'warning'
  return 'success'
}

export function reportTargetLabel(type: string): string {
  if (type === 'CHAT_MESSAGE') return '聊天消息'
  if (type === 'USER') return '聊天用户'
  if (type === 'PRODUCT') return '商品'
  return type
}

export function reportStatusLabel(status: string): string {
  if (status === 'PENDING') return '待处理'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'RESOLVED') return '已处理'
  if (status === 'REJECTED') return '已驳回'
  return status
}

export function formatCurrency(value?: number | string | null): string {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) return '¥0.00'
  return `¥${amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function formatCount(value?: number | null): string {
  const count = Number(value ?? 0)
  if (Number.isNaN(count)) return '0'
  return count.toLocaleString('zh-CN')
}
