import { defineStore } from 'pinia'
import { fetchCurrentAdmin, login, type AdminUser } from '@/api/auth'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    user: null as AdminUser | null,
  }),
  actions: {
    async login(username: string, password: string) {
      const result = await login(username, password)
      if (!result.success) {
        throw new Error(result.message ?? '登录失败')
      }
      localStorage.setItem('admin_token', result.data.token)
      this.user = result.data.user
    },
    async loadCurrentUser() {
      this.user = await fetchCurrentAdmin()
    },
    logout() {
      localStorage.removeItem('admin_token')
      this.user = null
    },
  },
})
