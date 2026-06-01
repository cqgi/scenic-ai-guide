<template>
  <footer class="composer">
    <div class="quick-actions">
      <button
        v-for="action in quickActions"
        :key="action"
        type="button"
        :disabled="disabled || busy"
        @click="$emit('quickAsk', action)"
      >
        {{ action }}
      </button>
      <button type="button" :disabled="disabled || busy" @click="$emit('route')">推荐路线</button>
    </div>

    <form class="input-line" @submit.prevent="submit">
      <input
        v-model="draft"
        :disabled="disabled || busy"
        placeholder="问我景点讲解、拍照点或游览路线"
        aria-label="输入景区问题"
      />
      <button class="send-button" type="submit" :disabled="disabled || busy || !draft.trim()">发送</button>
      <button
        class="voice-button"
        type="button"
        :class="{ recording }"
        :disabled="disabled || busy"
        :aria-label="recording ? '停止录音' : '开始录音'"
        @click="$emit('toggleRecording')"
      >
        <span v-if="recording" />
      </button>
    </form>

    <p class="hint">{{ hint }}</p>
  </footer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{
  disabled: boolean
  busy: boolean
  recording: boolean
}>()

const emit = defineEmits<{
  send: [query: string]
  quickAsk: [query: string]
  route: []
  toggleRecording: []
}>()

const draft = ref('')
const quickActions = ['红叶谷适合拍照吗', '古寺遗址有什么历史', '亲子游怎么安排']

const hint = computed(() => {
  if (props.recording) {
    return '正在录音，再点一次结束并发送'
  }
  if (props.busy) {
    return '小栖正在整理讲解'
  }
  if (props.disabled) {
    return '正在连接景区服务'
  }
  return '语音和文字都可以，回答会自动生成来源与反馈入口'
})

function submit() {
  const query = draft.value.trim()
  if (!query || props.disabled || props.busy) {
    return
  }
  draft.value = ''
  emit('send', query)
}
</script>

<style scoped>
.composer {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  background: rgba(15, 15, 26, 0.58);
  backdrop-filter: blur(24px);
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
  padding-bottom: 2px;
}

.quick-actions::-webkit-scrollbar {
  display: none;
}

.quick-actions button {
  flex: 0 0 auto;
  min-height: 32px;
  height: 32px;
  border: 1px solid rgba(245, 240, 232, 0.1);
  border-radius: 999px;
  padding: 6px 12px;
  color: rgba(245, 240, 232, 0.78);
  background: rgba(245, 240, 232, 0.05);
  cursor: pointer;
}

.input-line {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
  align-items: center;
  min-height: 52px;
}

input {
  width: 100%;
  min-height: 48px;
  border: 1px solid rgba(0, 212, 170, 0.18);
  border-radius: 8px;
  padding: 0 15px;
  color: #f5f0e8;
  background: rgba(26, 26, 46, 0.68);
  outline: none;
}

input:focus {
  border-color: rgba(0, 212, 170, 0.52);
  box-shadow: 0 0 0 3px rgba(0, 212, 170, 0.08);
}

button {
  font: inherit;
}

button:disabled,
input:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.send-button {
  min-height: 48px;
  border: 0;
  border-radius: 8px;
  padding: 0 18px;
  color: #0f0f1a;
  font-weight: 700;
  background: linear-gradient(135deg, #00d4aa, #c9a96e);
  cursor: pointer;
}

.voice-button {
  position: relative;
  width: 52px;
  height: 52px;
  border: 2px solid rgba(0, 212, 170, 0.35);
  border-radius: 50%;
  background: rgba(0, 212, 170, 0.08);
  box-shadow: 0 0 26px rgba(0, 212, 170, 0.14);
  cursor: pointer;
}

.voice-button::before {
  content: '';
  position: absolute;
  inset: 14px 18px 18px;
  border-radius: 999px 999px 8px 8px;
  background: #f5f0e8;
}

.voice-button::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 12px;
  width: 20px;
  height: 9px;
  border: 2px solid #f5f0e8;
  border-top: 0;
  border-radius: 0 0 999px 999px;
  transform: translateX(-50%);
}

.voice-button.recording {
  border-color: rgba(201, 169, 110, 0.72);
  animation: pulse 1.5s ease-out infinite;
}

.voice-button.recording span {
  position: absolute;
  inset: 18px;
  z-index: 2;
  border-radius: 4px;
  background: #e8553d;
}

.hint {
  min-height: 18px;
  margin: 0;
  color: rgba(245, 240, 232, 0.5);
  font-size: 12px;
}

@keyframes pulse {
  from {
    box-shadow: 0 0 0 0 rgba(201, 169, 110, 0.32);
  }
  to {
    box-shadow: 0 0 0 18px rgba(201, 169, 110, 0);
  }
}

@media (max-width: 620px) {
  .composer {
    padding: 12px;
  }

  .input-line {
    grid-template-columns: 1fr auto;
  }

  .send-button {
    padding: 0 14px;
  }

  .voice-button {
    grid-column: 2;
    width: 48px;
    height: 48px;
  }
}
</style>
