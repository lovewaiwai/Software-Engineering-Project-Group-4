import { apiClient } from './client'
import type { ApiResponse } from './types'

// ---- 类型定义 ----

export interface Order {
    id: number
    orderNo: string
    productId: number
    buyerId: number
    sellerId: number
    amount: number
    status: string
    tradeMode: string
    productTitle?: string
    productImageUrl?: string
    productStatus?: string
    createdAt: string
    completedAt?: string
}

export interface Payment {
    id: number
    paymentNo: string
    orderId: number
    amount: number
    status: string
    providerTradeNo?: string
    payUrl?: string
    paidAt?: string
}

export interface Delivery {
    id: number
    taskNo: string
    orderId: number
    stationId: number
    boxId: number
    stationName?: string
    boxNo?: string
    pickupCode: string
    status: string
    storedAt?: string
    pickedUpAt?: string
}

export interface Review {
    id: number
    orderId: number
    reviewerId: number
    revieweeId: number
    rating: number
    content?: string
    createdAt: string
}

// ---- 订单 API ----

export async function createOrder(productId: number, tradeMode: string, lockerStationId?: number) {
    const { data } = await apiClient.post<ApiResponse<Order>>('/orders', {
        productId,
        tradeMode,
        lockerStationId,
    })
    return data
}

export async function getOrder(orderId: number) {
    const { data } = await apiClient.get<ApiResponse<Order>>(`/orders/${orderId}`)
    return data
}

export async function listMyOrders(role: 'buyer' | 'seller' = 'buyer') {
    const { data } = await apiClient.get<ApiResponse<Order[]>>('/orders/my', { params: { role } })
    return data
}

export async function cancelOrder(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Order>>(`/orders/${orderId}/cancel`)
    return data
}

export async function confirmComplete(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Order>>(`/orders/${orderId}/complete`)
    return data
}

// ---- 支付 API ----

export async function createPayment(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Payment>>('/payments', { orderId })
    return data
}

export async function mockPayCallback(providerTradeNo: string) {
    const { data } = await apiClient.post<ApiResponse<Payment>>(`/payments/mock-callback/${providerTradeNo}`)
    return data
}

export async function refundPayment(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Payment>>(`/payments/refund/${orderId}`)
    return data
}

// ---- 柜机 API ----

export async function reserveLocker(orderId: number, stationId: number, size = 'M') {
    const { data } = await apiClient.post<ApiResponse<Delivery>>('/delivery/reserve', {
        orderId,
        stationId,
        size,
    })
    return data
}

export async function confirmStored(taskNo: string) {
    const { data } = await apiClient.post<ApiResponse<Delivery>>(`/delivery/${taskNo}/stored`)
    return data
}

export async function confirmPickedUp(taskNo: string, pickupCode: string) {
    const { data } = await apiClient.post<ApiResponse<Delivery>>(`/delivery/${taskNo}/pickup`, null, {
        params: { pickupCode },
    })
    return data
}

export async function getDeliveryByOrder(orderId: number) {
    const { data } = await apiClient.get<ApiResponse<Delivery>>(`/delivery/order/${orderId}`)
    return data
}

export async function getPaymentByOrder(orderId: number) {
    const { data } = await apiClient.get<ApiResponse<Payment>>(`/payments/order/${orderId}`)
    return data
}

// ---- 评价 API ----

export async function createReview(orderId: number, rating: number, content?: string) {
    const { data } = await apiClient.post<ApiResponse<Review>>('/reviews', { orderId, rating, content })
    return data
}

export async function getReviewsByOrder(orderId: number) {
    const { data } = await apiClient.get<ApiResponse<Review[]>>(`/reviews/order/${orderId}`)
    return data
}

export async function sellerConfirmOrder(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Order>>(`/orders/${orderId}/seller-confirm`)
    return data
}

export async function sellerRejectOrder(orderId: number) {
    const { data } = await apiClient.post<ApiResponse<Order>>(`/orders/${orderId}/seller-reject`)
    return data
}
