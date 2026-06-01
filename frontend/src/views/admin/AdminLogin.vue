<template>
  <main class="login-page">
    <el-card class="login-card">
      <h1>管理后台</h1>
      <p>景区导览服务 AI 数字人</p>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-alert v-if="error" type="error" :title="error" show-icon />
        <el-button type="primary" native-type="submit" :loading="loading" class="login-button">
          登录
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const error = ref('')
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await adminStore.login(form.username, form.password)
    await router.push('/admin/dashboard')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #eef2f7;
}

.login-card {
  width: min(420px, calc(100vw - 32px));
}

h1 {
  margin: 0 0 8px;
}

p {
  margin: 0 0 24px;
  color: #6b7280;
}

.login-button {
  width: 100%;
  margin-top: 16px;
}
</style>
