<template>
  <section ref="timelineRef" class="timeline" aria-label="导览对话">
    <article v-for="message in messages" :key="message.id" class="message" :class="message.role">
      <div class="bubble">
        <div v-if="message.asrText" class="asr-text">识别：{{ message.asrText }}</div>
        <p>{{ message.content }}</p>

        <div v-if="message.route" class="route-card">
          <div>
            <span>推荐路线</span>
            <strong>{{ message.route.name }}</strong>
          </div>
          <em>约 {{ message.route.durationMinutes }} 分钟</em>
          <ol>
            <li v-for="spot in message.route.spots" :key="spot.id">
              <span>{{ spot.name }}</span>
              <small>{{ spot.recommendedMinutes }} 分钟</small>
            </li>
          </ol>
          <p>{{ message.route.reason }}</p>
        </div>

        <div v-if="message.sources.length" class="sources">
          <span>来源</span>
          <button v-for="source in message.sources" :key="source.chunkId" type="button" :title="source.content">
            {{ source.title }}
          </button>
        </div>

        <div v-if="message.role === 'assistant' && message.interactionId" class="feedback">
          <span>{{ message.feedbackScore ? `已评价 ${message.feedbackScore} 分` : '满意度' }}</span>
          <button
            v-for="score in [1, 2, 3, 4, 5]"
            :key="score"
            type="button"
            :class="{ active: message.feedbackScore === score }"
            :disabled="Boolean(message.feedbackScore) || feedbackPendingId === message.id"
            @click="$emit('feedback', message.id, message.interactionId, score)"
          >
            {{ score }}
          </button>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import type { RouteRecommendation, SourceChunk } from '@/api/visitor'

export interface VisitorMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  interactionId?: number
  emotion?: string
  asrText?: string | null
  sources: SourceChunk[]
  route?: RouteRecommendation | null
  feedbackScore?: number
}

const props = defineProps<{
  messages: VisitorMessage[]
  feedbackPendingId: string | null
}>()

defineEmits<{
  feedback: [messageId: string, interactionId: number, score: number]
}>()

const timelineRef = ref<HTMLElement | null>(null)

watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    timelineRef.value?.scrollTo({ top: timelineRef.value.scrollHeight, behavior: 'smooth' })
  },
)
</script>

<style scoped>
.timeline {
  min-height: 0;
  overflow: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 212, 170, 0.3) transparent;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.system {
  justify-content: center;
}

.bubble {
  width: fit-content;
  max-width: min(680px, 86%);
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #f5f0e8;
  background: rgba(26, 26, 46, 0.62);
  backdrop-filter: blur(18px);
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.18);
}

.assistant .bubble {
  border-left: 2px solid rgba(201, 169, 110, 0.78);
  border-radius: 4px 16px 16px;
}

.user .bubble {
  border-right: 1px solid rgba(0, 212, 170, 0.34);
  border-radius: 16px 4px 16px 16px;
  background: rgba(0, 212, 170, 0.09);
}

.system .bubble {
  max-width: 560px;
  border-radius: 999px;
  color: rgba(245, 240, 232, 0.72);
  text-align: center;
  background: rgba(245, 240, 232, 0.05);
}

.bubble p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.68;
}

.asr-text {
  margin-bottom: 8px;
  color: #c9a96e;
  font-size: 13px;
}

.sources {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.sources span,
.feedback span,
.route-card span {
  color: rgba(245, 240, 232, 0.58);
  font-size: 12px;
}

.sources button {
  min-height: 28px;
  border: 1px solid rgba(201, 169, 110, 0.26);
  border-radius: 999px;
  padding: 4px 10px;
  color: #f5f0e8;
  background: rgba(201, 169, 110, 0.08);
  cursor: pointer;
}

.feedback {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 7px;
  margin-top: 14px;
}

.feedback button {
  width: 28px;
  height: 28px;
  border: 1px solid rgba(0, 212, 170, 0.22);
  border-radius: 50%;
  color: #f5f0e8;
  background: rgba(0, 212, 170, 0.08);
  cursor: pointer;
}

.feedback button.active {
  border-color: rgba(201, 169, 110, 0.7);
  background: rgba(201, 169, 110, 0.22);
}

.feedback button:disabled {
  cursor: default;
  opacity: 0.72;
}

.route-card {
  margin-top: 14px;
  padding: 12px;
  border: 1px solid rgba(201, 169, 110, 0.2);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(201, 169, 110, 0.11), rgba(0, 212, 170, 0.06)),
    rgba(15, 15, 26, 0.35);
}

.route-card div {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.route-card strong {
  display: block;
  margin-top: 4px;
  font-size: 18px;
}

.route-card em {
  display: inline-block;
  margin-top: 8px;
  color: #c9a96e;
  font-style: normal;
}

.route-card ol {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 10px 0 6px;
  margin: 4px 0;
  overflow-x: auto;
  list-style: none;
}

.route-card li {
  min-width: 82px;
  display: grid;
  gap: 4px;
}

.route-card li::before {
  content: '';
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: #c9a96e;
  box-shadow: 0 0 14px rgba(201, 169, 110, 0.4);
}

.route-card small {
  color: rgba(245, 240, 232, 0.55);
}

@media (max-width: 760px) {
  .timeline {
    padding: 14px;
  }

  .bubble {
    max-width: 94%;
  }
}
</style>
