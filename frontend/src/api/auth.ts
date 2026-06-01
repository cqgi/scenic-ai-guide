import { apiClient, type ApiResponse } from './client'

export interface AdminUser {
  id: number
  username: string
  displayName: string
  role: string
}

export interface LoginResponse {
  token: string
  user: AdminUser
}

export async function login(username: string, password: string) {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/admin/auth/login', { username, password })
  return response.data
}

export async function fetchCurrentAdmin() {
  const response = await apiClient.get<ApiResponse<AdminUser>>('/admin/auth/me')
  return response.data.data
}
