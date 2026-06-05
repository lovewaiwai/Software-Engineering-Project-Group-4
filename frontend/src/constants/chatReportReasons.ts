export interface ChatReportReason {
  value: string
  label: string
  desc: string
}

/** 闲鱼风格聊天举报原因 */
export const CHAT_REPORT_REASONS: ChatReportReason[] = [
  { value: 'FRAUD', label: '涉嫌诈骗', desc: '虚假交易、诱导转账、仿冒身份等' },
  { value: 'OFFSITE', label: '引导站外交易', desc: '要求脱离平台交易、索取隐私信息' },
  { value: 'HARASSMENT', label: '骚扰辱骂', desc: '人身攻击、恶意骚扰、威胁恐吓' },
  { value: 'PORNOGRAPHY', label: '低俗色情', desc: '发布涉黄、低俗或不雅内容' },
  { value: 'SPAM', label: '广告引流', desc: '刷屏广告、引流到站外平台' },
  { value: 'FAKE_INFO', label: '虚假信息', desc: '商品描述不实、虚假成色或价格' },
  { value: 'OTHER', label: '其他违规', desc: '以上未涵盖的其他违规行为' },
]

export function formatReportReason(value: string): string {
  const item = CHAT_REPORT_REASONS.find((r) => r.value === value)
  return item ? item.label : value
}
