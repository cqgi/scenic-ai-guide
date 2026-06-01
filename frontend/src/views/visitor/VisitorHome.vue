<template>
  <main class="visitor-page">
    <div class="mist mist-one" />
    <div class="mist mist-two" />

    <DigitalHumanStage
      :state="humanState"
      :emotion="currentEmotion"
      :scenic-name="scenicArea?.name ?? '景区导览'"
      :mouth-open="mouthOpen"
    />

    <section class="guide-panel">
      <header class="guide-header">
        <div>
          <p>AI 景区导览</p>
          <h2>{{ scenicArea?.name ?? '正在连接景区' }}</h2>
          <span>{{ scenicArea?.summary ?? '小栖正在准备景区资料。' }}</span>
        </div>
        <div class="session-pill">
          <strong>{{ session ? `#${session.sessionId}` : '创建中' }}</strong>
          <small>游客会话</small>
        </div>
      </header>

      <div class="spot-strip" aria-label="景点列表">
        <button
          v-for="spot in scenicArea?.spots ?? []"
          :key="spot.id"
          type="button"
          @click="askQuick(`${spot.name}有什么特色？`)"
        >
          {{ spot.name }}
        </button>
      </div>

      <div class="notice-slot">
        <p v-if="error" class="error-banner">{{ error }}</p>
      </div>

      <ChatTimeline :messages="messages" :feedback-pending-id="feedbackPendingId" @feedback="handleFeedback" />

      <VisitorComposer
        :disabled="!session"
        :busy="busy"
        :recording="recording"
        @send="sendText"
        @quick-ask="askQuick"
        @route="requestRoute"
        @toggle-recording="toggleRecording"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { fetchDefaultScenicArea, type ScenicArea } from '@/api/scenic'
import {
  createVisitorSession,
  recommendRoute,
  sendTextChat,
  sendVoiceChat,
  submitFeedback,
  type ChatResponse,
  type MouthCue,
  type RouteRecommendation,
  type VisitorSession,
} from '@/api/visitor'
import ChatTimeline, { type VisitorMessage } from '@/components/visitor/ChatTimeline.vue'
import DigitalHumanStage, { type DigitalHumanState } from '@/components/visitor/DigitalHumanStage.vue'
import VisitorComposer from '@/components/visitor/VisitorComposer.vue'

const defaultInterests = ['history', 'nature', 'photo']
const scenicArea = ref<ScenicArea | null>(null)
const session = ref<VisitorSession | null>(null)
const messages = ref<VisitorMessage[]>([])
const busy = ref(false)
const error = ref('')
const humanState = ref<DigitalHumanState>('idle')
const currentEmotion = ref('neutral')
const mouthOpen = ref(0)
const recording = ref(false)
const feedbackPendingId = ref<string | null>(null)

let audio: HTMLAudioElement | null = null
let mouthTimer: number | undefined
let fallbackMouthTimer: number | undefined
let mediaRecorder: MediaRecorder | null = null
let mediaStream: MediaStream | null = null
let recordedChunks: BlobPart[] = []

const apiOrigin = computed(() => {
  const base = import.meta.env.VITE_API_BASE_URL as string
  if (!base) {
    return ''
  }
  try {
    return new URL(base).origin
  } catch {
    return ''
  }
})

onMounted(async () => {
  await initialize()
})

onBeforeUnmount(() => {
  stopAudio()
  stopMouthTimers()
  stopMediaTracks()
})

async function initialize() {
  humanState.value = 'thinking'
  try {
    scenicArea.value = await fetchDefaultScenicArea()
    session.value = await createVisitorSession(scenicArea.value.id, defaultInterests, 'h5')
    messages.value.push({
      id: newId(),
      role: 'assistant',
      content: `你好，我是你的景区 AI 导游小栖。可以问我景点讲解、拍照建议，也可以让我推荐路线。`,
      sources: [],
    })
    humanState.value = 'idle'
  } catch (err) {
    showError(err, '无法连接景区服务')
  }
}

async function sendText(query: string) {
  if (!session.value || busy.value) {
    return
  }
  messages.value.push({ id: newId(), role: 'user', content: query, sources: [] })
  busy.value = true
  error.value = ''
  humanState.value = 'thinking'
  stopAudio()
  try {
    const response = await sendTextChat(session.value.sessionToken, query, defaultInterests)
    await appendAssistantResponse(response)
  } catch (err) {
    showError(err, '问答请求失败，请稍后重试')
  } finally {
    busy.value = false
  }
}

function askQuick(query: string) {
  void sendText(query)
}

async function requestRoute() {
  if (!session.value || busy.value) {
    return
  }
  busy.value = true
  error.value = ''
  humanState.value = 'thinking'
  messages.value.push({ id: newId(), role: 'user', content: '请按我的兴趣推荐一条游览路线', sources: [] })
  try {
    const route = await recommendRoute(session.value.sessionToken, defaultInterests, 100)
    const content = routeToText(route)
    messages.value.push({
      id: newId(),
      role: 'assistant',
      content,
      emotion: 'happy',
      sources: [],
      route,
    })
    currentEmotion.value = 'happy'
    humanState.value = 'happy'
    playFallbackMouth(content)
  } catch (err) {
    showError(err, '路线推荐失败，请稍后重试')
  } finally {
    busy.value = false
  }
}

async function toggleRecording() {
  if (recording.value) {
    mediaRecorder?.stop()
    return
  }
  if (!session.value || busy.value) {
    return
  }
  if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
    error.value = '当前浏览器不支持录音，请改用文字输入'
    humanState.value = 'error'
    return
  }
  try {
    error.value = ''
    recordedChunks = []
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(mediaStream, { mimeType: supportedMimeType() })
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        recordedChunks.push(event.data)
      }
    }
    mediaRecorder.onstop = () => {
      const blob = new Blob(recordedChunks, { type: supportedMimeType() })
      stopMediaTracks()
      recording.value = false
      void sendVoice(blob)
    }
    mediaRecorder.start()
    recording.value = true
    humanState.value = 'listening'
  } catch (err) {
    recording.value = false
    stopMediaTracks()
    showError(err, '无法获取麦克风权限')
  }
}

async function sendVoice(blob: Blob) {
  if (!session.value || busy.value) {
    return
  }
  busy.value = true
  humanState.value = 'thinking'
  try {
    const response = await sendVoiceChat(session.value.sessionToken, blob, defaultInterests)
    if (response.asrText) {
      messages.value.push({ id: newId(), role: 'user', content: response.asrText, sources: [] })
    }
    await appendAssistantResponse(response)
  } catch (err) {
    showError(err, '语音识别失败，请改用文字输入或稍后重试')
  } finally {
    busy.value = false
  }
}

async function appendAssistantResponse(response: ChatResponse) {
  const route = response.intent === 'route_recommendation' ? parseRouteFromAnswer(response.answer) : null
  const message: VisitorMessage = {
    id: newId(),
    role: 'assistant',
    content: response.answer,
    interactionId: response.interactionId,
    emotion: response.emotion,
    asrText: response.asrText,
    sources: response.sourceChunks ?? [],
    route,
  }
  messages.value.push(message)
  currentEmotion.value = response.emotion
  await playAnswer(response.answer, response.audioUrl, response.mouthCues)
}

async function playAnswer(answer: string, audioUrl: string | null, mouthCues: MouthCue[]) {
  stopAudio()
  if (!audioUrl) {
    humanState.value = currentEmotion.value === 'sorry' ? 'sorry' : 'speaking'
    playFallbackMouth(answer)
    return
  }
  humanState.value = 'speaking'
  const resolvedUrl = resolveAudioUrl(audioUrl)
  audio = new Audio(resolvedUrl)
  audio.onplay = () => runMouthCues(mouthCues)
  audio.onended = finishSpeaking
  audio.onerror = () => {
    error.value = '语音播放失败，已保留文字回答'
    playFallbackMouth(answer)
  }
  try {
    await audio.play()
  } catch {
    error.value = '浏览器阻止了自动播放，已保留文字回答'
    playFallbackMouth(answer)
  }
}

function runMouthCues(cues: MouthCue[]) {
  stopMouthTimers()
  const start = performance.now()
  mouthTimer = window.setInterval(() => {
    const seconds = (performance.now() - start) / 1000
    const cue = cues.find((item) => seconds >= item.start && seconds <= item.end)
    mouthOpen.value = cue ? cueValue(cue.value) : 0.12
  }, 70)
}

function playFallbackMouth(text: string) {
  stopMouthTimers()
  const duration = Math.min(4200, Math.max(1200, text.length * 45))
  const start = performance.now()
  fallbackMouthTimer = window.setInterval(() => {
    const elapsed = performance.now() - start
    if (elapsed >= duration) {
      finishSpeaking()
      return
    }
    mouthOpen.value = 0.25 + Math.abs(Math.sin(elapsed / 115)) * 0.75
  }, 90)
}

function finishSpeaking() {
  stopMouthTimers()
  mouthOpen.value = 0
  if (currentEmotion.value === 'happy') {
    humanState.value = 'happy'
    window.setTimeout(() => {
      if (!busy.value && !recording.value) humanState.value = 'idle'
    }, 1200)
    return
  }
  if (currentEmotion.value === 'sorry') {
    humanState.value = 'sorry'
    window.setTimeout(() => {
      if (!busy.value && !recording.value) humanState.value = 'idle'
    }, 1400)
    return
  }
  humanState.value = 'idle'
}

async function handleFeedback(messageId: string, interactionId: number, score: number) {
  if (!session.value || feedbackPendingId.value) {
    return
  }
  feedbackPendingId.value = messageId
  try {
    await submitFeedback(session.value.sessionToken, interactionId, score)
    const target = messages.value.find((message) => message.id === messageId)
    if (target) {
      target.feedbackScore = score
    }
  } catch (err) {
    showError(err, '反馈提交失败')
  } finally {
    feedbackPendingId.value = null
  }
}

function stopAudio() {
  if (audio) {
    audio.pause()
    audio.src = ''
    audio = null
  }
  stopMouthTimers()
  mouthOpen.value = 0
}

function stopMouthTimers() {
  if (mouthTimer) {
    window.clearInterval(mouthTimer)
    mouthTimer = undefined
  }
  if (fallbackMouthTimer) {
    window.clearInterval(fallbackMouthTimer)
    fallbackMouthTimer = undefined
  }
}

function stopMediaTracks() {
  mediaStream?.getTracks().forEach((track) => track.stop())
  mediaStream = null
}

function showError(err: unknown, fallback: string) {
  if (err instanceof Error && err.message.includes('timeout')) {
    error.value = '请求超时，请稍后重试或换一个更具体的问题'
  } else {
    error.value = err instanceof Error ? err.message : fallback
  }
  humanState.value = 'error'
  busy.value = false
}

function resolveAudioUrl(audioUrl: string) {
  if (audioUrl.startsWith('http')) {
    return audioUrl
  }
  return `${apiOrigin.value}${audioUrl}`
}

function supportedMimeType() {
  if (typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported('audio/webm')) {
    return 'audio/webm'
  }
  return ''
}

function cueValue(value: string) {
  const values: Record<string, number> = {
    A: 0.18,
    B: 0.45,
    C: 0.78,
    D: 1,
    E: 0.62,
    F: 0.3,
  }
  return values[value] ?? 0.4
}

function parseRouteFromAnswer(answer: string): RouteRecommendation | null {
  const name = answer.match(/推荐路线[:：](.+)/)?.[1]?.trim()
  const duration = Number(answer.match(/预计用时[:：](\d+)/)?.[1] ?? 0)
  const spotLine = answer.match(/途经景点[:：](.+)/)?.[1]?.trim()
  if (!name && !spotLine) {
    return null
  }
  return {
    routeId: 0,
    name: name || '推荐路线',
    durationMinutes: duration || 100,
    spots: (spotLine ? spotLine.split('、') : []).map((spot, index) => ({
      id: index + 1,
      name: spot,
      summary: '',
      recommendedMinutes: 20,
    })),
    reason: answer.match(/推荐理由[:：](.+)/)?.[1]?.trim() ?? '这条路线更符合你的兴趣。',
    guideText: answer.match(/讲解词[:：](.+)/)?.[1]?.trim() ?? '',
  }
}

function routeToText(route: RouteRecommendation) {
  return `推荐路线：${route.name}
预计用时：${route.durationMinutes} 分钟
途经景点：${route.spots.map((spot) => spot.name).join('、')}
推荐理由：${route.reason}
讲解词：${route.guideText}`
}

function newId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}
</script>

<style scoped>
.visitor-page {
  position: relative;
  height: 100vh;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 42vw) minmax(0, 1fr);
  overflow: hidden;
  color: #f5f0e8;
  background:
    radial-gradient(circle at 18% 20%, rgba(0, 212, 170, 0.14), transparent 28%),
    radial-gradient(circle at 82% 12%, rgba(201, 169, 110, 0.09), transparent 24%),
    linear-gradient(180deg, #0f0f1a 0%, #142334 52%, #10121f 100%);
}

.visitor-page::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.18;
  background-image:
    linear-gradient(rgba(245, 240, 232, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(245, 240, 232, 0.035) 1px, transparent 1px);
  background-size: 80px 80px;
}

.mist {
  position: absolute;
  width: 44vw;
  height: 44vw;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(70px);
  opacity: 0.16;
}

.mist-one {
  left: -16vw;
  top: 22vh;
  background: #00d4aa;
}

.mist-two {
  right: -18vw;
  bottom: -20vh;
  background: #c9a96e;
}

.guide-panel {
  position: relative;
  z-index: 2;
  min-width: 0;
  height: 100vh;
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr) auto;
  border-left: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(15, 15, 26, 0.34);
  backdrop-filter: blur(12px);
}

.guide-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 28px 28px 18px;
}

.guide-header p {
  margin: 0 0 8px;
  color: #00d4aa;
  font-weight: 700;
}

.guide-header h2 {
  margin: 0;
  font-size: clamp(26px, 3vw, 42px);
  line-height: 1.1;
  font-family: Georgia, 'Songti SC', serif;
}

.guide-header span {
  display: block;
  max-width: 760px;
  margin-top: 12px;
  color: rgba(245, 240, 232, 0.66);
  line-height: 1.7;
}

.session-pill {
  flex: 0 0 auto;
  min-width: 112px;
  height: fit-content;
  padding: 10px 12px;
  border: 1px solid rgba(0, 212, 170, 0.18);
  border-radius: 8px;
  text-align: right;
  background: rgba(0, 212, 170, 0.07);
}

.session-pill strong,
.session-pill small {
  display: block;
}

.session-pill small {
  margin-top: 4px;
  color: rgba(245, 240, 232, 0.52);
}

.spot-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
  padding: 0 28px 14px;
}

.spot-strip::-webkit-scrollbar {
  display: none;
}

.spot-strip button {
  flex: 0 0 auto;
  min-height: 32px;
  border: 1px solid rgba(201, 169, 110, 0.2);
  border-radius: 999px;
  padding: 5px 12px;
  color: rgba(245, 240, 232, 0.78);
  background: rgba(201, 169, 110, 0.07);
  cursor: pointer;
}

.error-banner {
  margin: 0;
  padding: 10px 12px;
  border: 1px solid rgba(232, 85, 61, 0.34);
  border-radius: 8px;
  color: #ffe5df;
  background: rgba(232, 85, 61, 0.12);
}

.notice-slot {
  min-height: 0;
  padding: 0 28px;
}

@media (max-width: 980px) {
  .visitor-page {
    grid-template-columns: 1fr;
    height: auto;
    overflow: auto;
  }

  .guide-panel {
    height: auto;
    min-height: 58vh;
    border-left: 0;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
  }
}

@media (max-width: 620px) {
  .guide-header {
    display: grid;
    padding: 20px 16px 14px;
  }

  .session-pill {
    width: 100%;
    text-align: left;
  }

  .spot-strip {
    padding: 0 16px 12px;
  }

  .error-banner {
    margin: 0;
  }

  .notice-slot {
    padding: 0 16px;
  }
}
</style>
