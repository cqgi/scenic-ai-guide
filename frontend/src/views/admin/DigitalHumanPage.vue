<template>
  <section class="digital-human-page">
    <el-card shadow="never">
      <template #header>数字人配置</template>
      <el-skeleton v-if="loading" :rows="8" animated />
      <el-form v-else label-position="top" class="profile-form">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="64" />
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input v-model="form.welcomeText" type="textarea" :rows="3" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="音色编码">
          <el-input v-model="form.voiceCode" placeholder="如 alloy / shimmer / vendor-voice-id" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.avatarUrl" placeholder="可填外部图片 URL，留空使用默认小栖形象" />
        </el-form-item>
        <el-form-item label="模型 URL">
          <el-input v-model="form.modelUrl" placeholder="预留 Live2D/3D 模型地址" />
        </el-form-item>
        <el-form-item label="服装风格">
          <el-input v-model="form.clothingStyle" placeholder="如 新中式导游服" />
        </el-form-item>
        <el-form-item label="人设 Prompt">
          <el-input v-model="form.personaPrompt" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item>
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存配置</el-button>
      </el-form>
    </el-card>

    <el-card shadow="never" class="preview-card">
      <template #header>游客端预览</template>
      <div class="human-preview">
        <div class="avatar-frame">
          <img :src="form.avatarUrl || xiaoqiImage" alt="数字人预览" />
        </div>
        <strong>{{ form.name || '小栖' }}</strong>
        <p>{{ form.welcomeText || '欢迎语将在游客端展示。' }}</p>
        <el-tag :type="form.enabled ? 'success' : 'info'">{{ form.enabled ? '游客端启用' : '游客端停用' }}</el-tag>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDigitalHumanProfile, updateDigitalHumanProfile, type DigitalHumanProfile } from '@/api/admin'
import xiaoqiImage from '@/assets/digital-human/xiaoqi-guide.png'

const loading = ref(false)
const saving = ref(false)
const form = reactive<DigitalHumanProfile>({
  id: 0,
  scenicAreaId: 1,
  scenicAreaName: '',
  name: '小栖',
  avatarUrl: null,
  modelUrl: null,
  voiceCode: null,
  clothingStyle: null,
  welcomeText: '你好，我是你的景区 AI 导游小栖。可以问我景点讲解、拍照建议，也可以让我推荐路线。',
  personaPrompt: '你是专业、亲切、可信的景区 AI 导游。',
  enabled: true,
})

onMounted(loadProfile)

async function loadProfile() {
  loading.value = true
  try {
    Object.assign(form, await fetchDigitalHumanProfile())
  } catch {
    ElMessage.error('数字人配置加载失败')
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  if (!form.name.trim() || !form.welcomeText.trim() || !form.personaPrompt.trim()) {
    ElMessage.warning('名称、欢迎语和人设 Prompt 不能为空')
    return
  }
  saving.value = true
  try {
    Object.assign(form, await updateDigitalHumanProfile(form))
    ElMessage.success('数字人配置已保存')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.digital-human-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.profile-form {
  max-width: 760px;
}

.preview-card {
  position: sticky;
  top: 88px;
}

.human-preview {
  display: grid;
  justify-items: center;
  gap: 14px;
  text-align: center;
}

.avatar-frame {
  width: 220px;
  aspect-ratio: 0.78;
  display: grid;
  place-items: end center;
  border-radius: 48% 48% 18px 18px;
  overflow: hidden;
  background:
    radial-gradient(circle at 50% 30%, rgba(0, 168, 132, 0.16), transparent 38%),
    linear-gradient(180deg, #eef6f3, #dbeafe);
}

.avatar-frame img {
  width: 92%;
  max-height: 100%;
  object-fit: contain;
}

.human-preview strong {
  color: #111827;
  font-size: 26px;
}

.human-preview p {
  margin: 0;
  color: #4b5563;
  line-height: 1.7;
}

@media (max-width: 980px) {
  .digital-human-page {
    grid-template-columns: 1fr;
  }

  .preview-card {
    position: static;
  }
}
</style>
