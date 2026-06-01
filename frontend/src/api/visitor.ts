import { apiClient, type ApiResponse } from './client'

export interface VisitorSession {
  sessionId: number
  sessionToken: string
  interests: string
}

export interface MouthCue {
  start: number
  end: number
  value: string
}

export interface SourceChunk {
  chunkId: number
  documentId: number
  title: string
  content: string
  finalScore: number
}

export interface ChatResponse {
  interactionId: number
  intent: string
  answer: string
  emotion: 'neutral' | 'happy' | 'sorry' | 'thinking' | string
  sourceChunks: SourceChunk[]
  audioUrl: string | null
  mouthCues: MouthCue[]
  rejected: boolean
  asrText: string | null
}

export interface RouteSpot {
  id: number
  name: string
  summary: string
  recommendedMinutes: number
}

export interface RouteRecommendation {
  routeId: number
  name: string
  durationMinutes: number
  spots: RouteSpot[]
  reason: string
  guideText: string
}

export interface FeedbackResponse {
  feedbackId: number
  interactionId: number
  score: number
}

export async function createVisitorSession(scenicAreaId: number, interests: string[], deviceType = 'web') {
  const response = await apiClient.post<ApiResponse<VisitorSession>>('/visitor/sessions', {
    scenicAreaId,
    interests,
    deviceType,
  })
  return response.data.data
}

export async function sendTextChat(sessionToken: string, query: string, interests: string[]) {
  const response = await apiClient.post<ApiResponse<ChatResponse>>('/visitor/chat/text', {
    sessionToken,
    query,
    interests,
  })
  return response.data.data
}

export async function sendVoiceChat(sessionToken: string, audio: Blob, interests: string[]) {
  const form = new FormData()
  form.append('sessionToken', sessionToken)
  interests.forEach((interest) => form.append('interests', interest))
  form.append('audio', audio, 'visitor-question.webm')
  const response = await apiClient.post<ApiResponse<ChatResponse>>('/visitor/chat/voice', form)
  return response.data.data
}

export async function recommendRoute(sessionToken: string, interests: string[], durationMinutes?: number) {
  const response = await apiClient.post<ApiResponse<RouteRecommendation>>('/visitor/routes/recommend', {
    sessionToken,
    interests,
    durationMinutes,
  })
  return response.data.data
}

export async function submitFeedback(sessionToken: string, interactionId: number, score: number, comment?: string) {
  const response = await apiClient.post<ApiResponse<FeedbackResponse>>('/visitor/feedback', {
    sessionToken,
    interactionId,
    score,
    comment,
  })
  return response.data.data
}
