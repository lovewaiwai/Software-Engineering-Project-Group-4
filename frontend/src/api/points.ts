import { apiClient } from './client'
import type {
  ApiResponse,
  PageResponse,
  PointItem,
  PointRecord,
  PointRedemption,
  PointTask,
} from './types'

export async function fetchPointTasks() {
  const { data } = await apiClient.get<ApiResponse<PointTask[]>>('/points/tasks')
  return data
}

export async function checkIn() {
  const { data } = await apiClient.post<ApiResponse<PointRecord>>('/points/check-in')
  return data
}

export async function claimPointTask(code: string) {
  const { data } = await apiClient.post<ApiResponse<PointRecord>>(`/points/tasks/${code}/claim`)
  return data
}

export async function fetchPointRecords(page = 1, pageSize = 20) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<PointRecord>>>('/points/records', {
    params: { page, pageSize },
  })
  return data
}

export async function fetchPointItems() {
  const { data } = await apiClient.get<ApiResponse<PointItem[]>>('/points/items')
  return data
}

export async function redeemPointItem(itemCode: string) {
  const { data } = await apiClient.post<ApiResponse<PointRedemption>>('/points/redeem', { itemCode })
  return data
}
