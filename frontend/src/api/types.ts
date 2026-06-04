export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface ModuleHealth {
  module: string
  status: string
}
