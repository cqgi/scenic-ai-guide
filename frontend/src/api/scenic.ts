import { apiClient, type ApiResponse } from './client'

export interface ScenicSpot {
  id: number
  name: string
  alias: string | null
  summary: string
  tags: string
  recommendedMinutes: number
}

export interface ScenicArea {
  id: number
  name: string
  summary: string
  location: string
  openingHours: string
  contactPhone: string
  spots: ScenicSpot[]
}

export async function fetchDefaultScenicArea() {
  const response = await apiClient.get<ApiResponse<ScenicArea>>('/scenic-areas/default')
  return response.data.data
}
