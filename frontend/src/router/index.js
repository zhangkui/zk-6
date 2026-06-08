import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/monitor'
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('../views/Monitor.vue'),
    meta: { title: '停车场余位监测' }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('../views/History.vue'),
    meta: { title: '历史车流分析' }
  },
  {
    path: '/prediction',
    name: 'Prediction',
    component: () => import('../views/Prediction.vue'),
    meta: { title: '分时段热度预测' }
  },
  {
    path: '/warning',
    name: 'Warning',
    component: () => import('../views/Warning.vue'),
    meta: { title: '拥堵预警展示' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '城市停车热度预测平台'
  next()
})

export default router
