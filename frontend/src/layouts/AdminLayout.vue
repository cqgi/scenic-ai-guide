<template>
  <el-container class="admin-shell">
    <el-aside width="232px">
      <div class="brand">AI 导览后台</div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/admin/dashboard">数据大屏</el-menu-item>
        <el-menu-item index="/admin/knowledge">知识库管理</el-menu-item>
        <el-menu-item index="/admin/interactions">交互记录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>{{ adminStore.user?.displayName ?? '景区管理员' }}</span>
        <el-button text @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

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
}

.el-aside {
  background: #111827;
  color: white;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-weight: 700;
}

.el-menu {
  border-right: 0;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
}

.el-main {
  background: #f6f7fb;
}
</style>
