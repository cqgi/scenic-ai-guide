<template>
  <el-container class="admin-shell">
    <el-aside width="236px" class="admin-sidebar">
      <div class="brand">
        <span class="brand-mark">AI</span>
        <div>
          <strong>景区导览后台</strong>
          <small>运营控制台</small>
        </div>
      </div>
      <el-menu router :default-active="$route.path" background-color="transparent" text-color="#a7b0c0" active-text-color="#ffffff">
        <el-menu-item index="/admin/dashboard">数据大屏</el-menu-item>
        <el-menu-item index="/admin/knowledge">知识库管理</el-menu-item>
        <el-menu-item index="/admin/digital-human">数字人配置</el-menu-item>
        <el-menu-item index="/admin/interactions">交互记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div>
          <strong>{{ pageTitle }}</strong>
          <small>知识、问答、数字人和服务数据统一管理</small>
        </div>
        <div class="header-actions">
          <span>{{ adminStore.user?.displayName ?? '景区管理员' }}</span>
          <el-button text @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/dashboard': '数据大屏',
    '/admin/knowledge': '知识库管理',
    '/admin/digital-human': '数字人配置',
    '/admin/interactions': '交互记录',
  }
  return titles[route.path] ?? '管理后台'
})

onMounted(() => {
  if (!adminStore.user) {
    adminStore.loadCurrentUser().catch(() => {
      adminStore.logout()
      router.push('/admin/login')
    })
  }
})

function logout() {
  adminStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: #f4f7fb;
}

.admin-sidebar {
  background: linear-gradient(180deg, #101827 0%, #0b1220 100%);
  color: white;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.brand {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #00a884;
  font-weight: 800;
  color: #fff;
}

.brand strong,
.brand small,
.admin-header strong,
.admin-header small {
  display: block;
}

.brand small {
  margin-top: 3px;
  color: #7f8da3;
  font-size: 12px;
}

.el-menu {
  border-right: 0;
  padding: 8px;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
}

:deep(.el-menu-item.is-active) {
  background: rgba(0, 168, 132, 0.18);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  height: 68px;
  padding: 0 28px;
}

.admin-header strong {
  color: #111827;
  font-size: 18px;
}

.admin-header small {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #4b5563;
}

.admin-main {
  min-width: 0;
  background: #f4f7fb;
  padding: 22px;
}

@media (max-width: 760px) {
  .admin-shell {
    display: block;
  }

  .admin-sidebar {
    width: 100% !important;
  }

  .admin-header {
    padding: 0 16px;
  }

  .admin-main {
    padding: 16px;
  }
}
</style>
