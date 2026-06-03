import { apiClient } from './client'
import type { ApiResponse, ModuleHealth } from './types'

export function getModuleHealth(moduleName: string) {
  return apiClient.get<ApiResponse<ModuleHealth>>(`/${moduleName}/health`)
}
