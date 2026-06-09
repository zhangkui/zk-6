import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '实时监测' }
  },
  {
    path: '/traffic',
    name: 'Traffic',
    component: () => import('@/views/TrafficAnalysis.vue'),
    meta: { title: '历史车流分析' }
  },
  {
    path: '/prediction',
    name: 'Prediction',
    component: () => import('@/views/HeatPrediction.vue'),
    meta: { title: '热度预测' }
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: () => import('@/views/CongestionAlerts.vue'),
    meta: { title: '拥堵预警' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 城市停车热度预测平台` : '城市停车热度预测平台'
  next()
})

export default router
