import { apiClient, type ApiResponse } from './client'

export interface KnowledgeDocument {
  id: number
  scenicAreaId: number
  title: string
  fileName: string | null
  fileUrl: string | null
  docType: string
  status: 'active' | 'disabled' | string
  chunkCount: number
  enabledChunkCount: number
  createdAt: string
  updatedAt: string
}

export interface KnowledgeChunk {
  id: number
  documentId: number
  scenicSpotId: number | null
  scenicSpotName: string | null
  title: string
  content: string
  summary: string | null
  keywords: string | null
  aliases: string | null
  spotNames: string | null
  tags: string | null
  metadataJson: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface DigitalHumanProfile {
  id: number
  scenicAreaId: number
  scenicAreaName: string
  name: string
  avatarUrl: string | null
  modelUrl: string | null
  voiceCode: string | null
  clothingStyle: string | null
  welcomeText: string
  personaPrompt: string
  enabled: boolean
}

export interface InteractionSummary {
  id: number
  sessionId: number
  sessionToken: string
  scenicAreaName: string
  inputType: string
  userQuery: string
  asrText: string | null
  intent: string
  answer: string
  emotion: string
  sourceChunkIds: string | null
  latencyMs: number | null
  satisfaction: number | null
  createdAt: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
}

export interface RetrievalCandidate {
  chunkId: number
  documentId: number
  title: string
  content: string
  sourceType: string
  bm25Score: number
  keywordScore: number
  vectorScore: number
  metadataBoost: number
  finalScore: number
  hitReasons: string[]
}

export interface RetrievalTrace {
  id: number
  interactionId: number | null
  query: string
  bm25Candidates: RetrievalCandidate[]
  keywordCandidates: RetrievalCandidate[]
  vectorCandidates: RetrievalCandidate[]
  finalCandidates: RetrievalCandidate[]
  rejected: boolean
  rejectReason: string | null
  createdAt: string
}

export interface SourceChunkDetail {
  id: number
  documentId: number
  documentTitle: string
  title: string
  content: string
  summary: string | null
  keywords: string | null
  spotNames: string | null
  enabled: boolean
}

export interface FeedbackDetail {
  id: number
  score: number
  comment: string | null
  sentiment: string | null
  createdAt: string
}

export interface InteractionDetail {
  interaction: InteractionSummary
  sourceChunks: SourceChunkDetail[]
  feedback: FeedbackDetail | null
  trace: RetrievalTrace | null
}

export interface DashboardOverview {
  totalInteractions: number
  todayInteractions: number
  routeRequests: number
  activeDocuments: number
  averageSatisfaction: number
  rejectRate: number
  averageLatencyMs: number
  serviceAdvice: string[]
}

export interface DashboardTrends {
  daily: Array<{ date: string; interactions: number; averageSatisfaction: number | null }>
  emotions: Record<string, number>
}

export interface HotQuestion {
  query: string
  count: number
}

export async function fetchKnowledgeDocuments(scenicAreaId = 1) {
  const response = await apiClient.get<ApiResponse<KnowledgeDocument[]>>('/admin/knowledge/documents', {
    params: { scenicAreaId },
  })
  return response.data.data
}

export async function uploadKnowledgeDocument(scenicAreaId: number, title: string, file: File) {
  const form = new FormData()
  form.append('scenicAreaId', String(scenicAreaId))
  form.append('title', title)
  form.append('file', file)
  const response = await apiClient.post<ApiResponse<{ documentId: number; chunkCount: number; chunkIds: number[] }>>(
    '/admin/knowledge/documents',
    form,
  )
  return response.data.data
}

export async function updateKnowledgeDocumentStatus(id: number, status: string) {
  const response = await apiClient.patch<ApiResponse<KnowledgeDocument>>(`/admin/knowledge/documents/${id}/status`, {
    status,
  })
  return response.data.data
}

export async function deleteKnowledgeDocument(id: number) {
  await apiClient.delete<ApiResponse<null>>(`/admin/knowledge/documents/${id}`)
}

export async function fetchKnowledgeChunks(documentId: number) {
  const response = await apiClient.get<ApiResponse<KnowledgeChunk[]>>(`/admin/knowledge/documents/${documentId}/chunks`)
  return response.data.data
}

export async function updateKnowledgeChunkEnabled(id: number, enabled: boolean) {
  const response = await apiClient.patch<ApiResponse<KnowledgeChunk>>(`/admin/knowledge/chunks/${id}/enabled`, {
    enabled,
  })
  return response.data.data
}

export async function fetchDigitalHumanProfile() {
  const response = await apiClient.get<ApiResponse<DigitalHumanProfile>>('/admin/digital-human/default')
  return response.data.data
}

export async function fetchPublicDigitalHumanProfile() {
  const response = await apiClient.get<ApiResponse<DigitalHumanProfile>>('/digital-human/default')
  return response.data.data
}

export async function updateDigitalHumanProfile(profile: DigitalHumanProfile) {
  const response = await apiClient.put<ApiResponse<DigitalHumanProfile>>('/admin/digital-human/default', profile)
  return response.data.data
}

export async function fetchInteractions(params: {
  page: number
  size: number
  keyword?: string
  intent?: string
  emotion?: string
  satisfaction?: number | null
}) {
  const response = await apiClient.get<ApiResponse<PageResponse<InteractionSummary>>>('/admin/interactions', { params })
  return response.data.data
}

export async function fetchInteractionDetail(id: number) {
  const response = await apiClient.get<ApiResponse<InteractionDetail>>(`/admin/interactions/${id}`)
  return response.data.data
}

export async function fetchDashboardOverview() {
  const response = await apiClient.get<ApiResponse<DashboardOverview>>('/admin/dashboard/overview')
  return response.data.data
}

export async function fetchDashboardTrends(days = 7) {
  const response = await apiClient.get<ApiResponse<DashboardTrends>>('/admin/dashboard/trends', { params: { days } })
  return response.data.data
}

export async function fetchHotQuestions(limit = 10) {
  const response = await apiClient.get<ApiResponse<HotQuestion[]>>('/admin/dashboard/hot-questions', {
    params: { limit },
  })
  return response.data.data
}
