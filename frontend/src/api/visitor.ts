import { apiClient, type ApiResponse } from './client'

export interface VisitorSession {
  sessionId: number
  sessionToken: string
  interests: string
}

export async function createVisitorSession(scenicAreaId: number, interests: string[], deviceType = 'web') {
  const response = await apiClient.post<ApiResponse<VisitorSession>>('/visitor/sessions', {
    scenicAreaId,
    interests,
    deviceType,
  })
  return response.data.data
}
