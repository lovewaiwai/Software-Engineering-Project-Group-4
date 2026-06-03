export const productStatuses = [
  'DRAFT',
  'PENDING_REVIEW',
  'ACTIVE',
  'REVIEW_REJECTED',
  'LOCKED',
  'SOLD',
  'OFFLINE',
] as const

export const orderStatuses = [
  'CREATED',
  'SELLER_CONFIRMED',
  'PAYMENT_PENDING',
  'PAID',
  'DELIVERY_PENDING',
  'RECEIVED',
  'COMPLETED',
  'CANCELLED',
  'REFUNDING',
  'REFUNDED',
  'DISPUTED',
  'CLOSED',
] as const
