<template>
  <main class="visitor-page">
    <section class="digital-human">
      <div class="avatar">
        <span>小栖</span>
      </div>
      <div class="status">待机中</div>
    </section>

    <section class="guide-panel">
      <header>
        <p class="eyebrow">AI 景区导览</p>
        <h1>{{ scenicArea?.name ?? '景区导览服务 AI 数字人' }}</h1>
        <p>{{ scenicArea?.summary ?? '正在连接景区服务...' }}</p>
      </header>

      <el-alert v-if="error" type="error" :title="error" show-icon />

      <div class="spot-list">
        <el-tag v-for="spot in scenicArea?.spots ?? []" :key="spot.id" effect="plain">
          {{ spot.name }}
        </el-tag>
      </div>

      <div class="chat-box">
        <div class="message assistant">
          你好，我是你的景区 AI 导游。第一阶段已接入景区信息和游客匿名会话。
        </div>
      </div>

      <footer class="input-row">
        <el-input v-model="draft" placeholder="后续将在这里输入景区问题" size="large" />
        <el-button type="primary" size="large" :disabled="!session">发送</el-button>
      </footer>

      <p class="session-line">
        游客会话：
        <span>{{ session ? `#${session.sessionId}` : '创建中...' }}</span>
      </p>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchDefaultScenicArea, type ScenicArea } from '@/api/scenic'
import { createVisitorSession, type VisitorSession } from '@/api/visitor'

const scenicArea = ref<ScenicArea | null>(null)
const session = ref<VisitorSession | null>(null)
const draft = ref('')
const error = ref('')

onMounted(async () => {
  try {
    scenicArea.value = await fetchDefaultScenicArea()
    session.value = await createVisitorSession(scenicArea.value.id, ['history', 'nature'])
  } catch (err) {
    error.value = err instanceof Error ? err.message : '无法连接后端服务'
  }
})
</script>

<style scoped>
.visitor-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(280px, 420px) 1fr;
  background: linear-gradient(135deg, #eef7f0 0%, #f7f3ea 45%, #eef2f7 100%);
}

.digital-human {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
  padding: 32px;
  border-right: 1px solid rgba(31, 41, 55, 0.08);
}

.avatar {
  width: min(64vw, 280px);
  aspect-ratio: 1;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 50% 36%, #ffffff 0 24%, #b7d7bd 25% 52%, #315f4d 53% 100%);
  box-shadow: 0 24px 60px rgba(49, 95, 77, 0.22);
}

.avatar span {
  margin-top: 120px;
  color: white;
  font-size: 32px;
  font-weight: 700;
}

.status,
.session-line {
  color: #4b5563;
}

.guide-panel {
  max-width: 920px;
  width: 100%;
  margin: 0 auto;
  padding: 56px 40px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.eyebrow {
  color: #315f4d;
  font-weight: 700;
  margin: 0 0 8px;
}

h1 {
  margin: 0 0 12px;
  font-size: 36px;
}

header p:last-child {
  max-width: 680px;
  color: #4b5563;
  line-height: 1.7;
}

.spot-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chat-box {
  min-height: 260px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(31, 41, 55, 0.08);
  border-radius: 8px;
}

.message {
  max-width: 560px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #ffffff;
  line-height: 1.6;
}

.input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

@media (max-width: 760px) {
  .visitor-page {
    grid-template-columns: 1fr;
  }

  .digital-human {
    min-height: 300px;
    border-right: 0;
    border-bottom: 1px solid rgba(31, 41, 55, 0.08);
  }

  .guide-panel {
    padding: 32px 20px;
  }
}
</style>
