import { apiClient } from './client'
import type { ApiResponse, PageResponse } from './types'

export interface CategoryItem {
  id: number
  parentId?: number
  name: string
  sortOrder?: number
  children?: CategoryItem[]
}

export interface TagItem {
  id: number
  name: string
}

export interface ProductItem {
  id: number
  sellerId: number
  categoryId: number
  categoryName?: string
  title: string
  description?: string
  price: number
  originalPrice?: number
  conditionLevel: string
  campus?: string
  tradeModes: string[]
  status: string
  viewCount?: number
  favoriteCount?: number
  auditReason?: string
  createdAt?: string
  updatedAt?: string
  imageUrls?: string[]
  favorited?: boolean
  recommendReason?: string
}

export interface ProductSearchParams {
  keyword?: string
  categoryId?: number
  minPrice?: number
  maxPrice?: number
  conditionLevel?: string
  campus?: string
  tradeMode?: string
  status?: string
  sort?: string
  page?: number
  pageSize?: number
}

export interface ProductPayload {
  categoryId: number
  title: string
  description?: string
  price: number
  originalPrice?: number
  conditionLevel: string
  campus?: string
  tradeModes: string[]
  imageUrls: string[]
}

export interface AiProductSuggestionRequest {
  title: string
  description?: string
  conditionLevel?: string
}

export interface AiProductSuggestion {
  logId: number
  suggestedCategoryId?: number
  suggestedCategoryName?: string
  suggestedTags: string[]
  suggestedMinPrice?: number
  suggestedMaxPrice?: number
  provider?: string
  status?: string
}

export async function listCategories() {
  const { data } = await apiClient.get<ApiResponse<CategoryItem[]>>('/categories')
  return data
}

export async function listTags() {
  const { data } = await apiClient.get<ApiResponse<TagItem[]>>('/tags')
  return data
}

export async function searchProducts(params: ProductSearchParams) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ProductItem>>>('/products', { params })
  return data
}

export async function listMyProducts(params: ProductSearchParams) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<ProductItem>>>('/products/mine', { params })
  return data
}

export async function getProduct(id: number) {
  const { data } = await apiClient.get<ApiResponse<ProductItem>>(`/products/${id}`)
  return data
}

export async function createProduct(payload: ProductPayload) {
  const { data } = await apiClient.post<ApiResponse<ProductItem>>('/products', payload)
  return data
}

export async function updateProduct(id: number, payload: ProductPayload) {
  const { data } = await apiClient.put<ApiResponse<ProductItem>>(`/products/${id}`, payload)
  return data
}

export async function addProductImage(id: number, payload: { url: string; sortOrder?: number }) {
  const { data } = await apiClient.post<ApiResponse<ProductItem>>(`/products/${id}/images`, payload)
  return data
}

export async function uploadProductImage(file: File) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await apiClient.post<ApiResponse<{ url: string }>>('/chat/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function favoriteProduct(id: number) {
  const { data } = await apiClient.post<ApiResponse<Record<string, never>>>(`/products/${id}/favorite`)
  return data
}

export async function unfavoriteProduct(id: number) {
  const { data } = await apiClient.delete<ApiResponse<Record<string, never>>>(`/products/${id}/favorite`)
  return data
}

export async function recordProductView(id: number) {
  const { data } = await apiClient.post<ApiResponse<Record<string, never>>>(`/products/${id}/view`)
  return data
}

export async function listRecommendations(limit = 20) {
  const { data } = await apiClient.get<ApiResponse<ProductItem[]>>('/recommendations', { params: { limit } })
  return data
}

export async function suggestProduct(payload: AiProductSuggestionRequest) {
  const { data } = await apiClient.post<ApiResponse<AiProductSuggestion>>('/ai/product-suggestions', payload)
  return data
}
