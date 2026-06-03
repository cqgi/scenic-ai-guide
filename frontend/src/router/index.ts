import { createRouter, createWebHistory } from 'vue-router'
import VisitorHome from '@/views/visitor/VisitorHome.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminLogin from '@/views/admin/AdminLogin.vue'
import AdminDashboard from '@/views/admin/AdminDashboard.vue'
import KnowledgePage from '@/views/admin/KnowledgePage.vue'
import InteractionsPage from '@/views/admin/InteractionsPage.vue'
import DigitalHumanPage from '@/views/admin/DigitalHumanPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/visitor' },
    { path: '/visitor', component: VisitorHome },
    { path: '/admin/login', component: AdminLogin },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: AdminDashboard },
        { path: 'knowledge', component: KnowledgePage },
        { path: 'digital-human', component: DigitalHumanPage },
        { path: 'interactions', component: InteractionsPage },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && to.path !== '/admin/login' && !localStorage.getItem('admin_token')) {
    return '/admin/login'
  }
})

export default router
