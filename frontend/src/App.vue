<template>
  <div class="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon size="32" color="#409EFF"><Location /></el-icon>
          <h1 class="app-title">城市停车热度预测与可视化平台</h1>
        </div>
        <div class="header-right">
          <el-text class="current-time">{{ currentTime }}</el-text>
        </div>
      </el-header>
      <el-container>
        <el-aside width="220px" class="app-aside">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
            background-color="#001529"
            text-color="#b8c5d1"
            active-text-color="#ffffff"
          >
            <el-menu-item index="/dashboard">
              <el-icon><DataBoard /></el-icon>
              <span>实时监测</span>
            </el-menu-item>
            <el-menu-item index="/traffic">
              <el-icon><TrendCharts /></el-icon>
              <span>历史车流分析</span>
            </el-menu-item>
            <el-menu-item index="/prediction">
              <el-icon><Histogram /></el-icon>
              <span>热度预测</span>
            </el-menu-item>
            <el-menu-item index="/alerts">
              <el-icon><Warning /></el-icon>
              <span>拥堵预警</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="app-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const currentTime = ref('')
let timer = null

const activeMenu = computed(() => route.path)

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const handleMenuSelect = (index) => {
  router.push(index)
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-header {
  background: linear-gradient(90deg, #001529 0%, #002140 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid #1890ff;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .app-title {
      color: #ffffff;
      font-size: 20px;
      font-weight: 600;
      margin: 0;
      letter-spacing: 2px;
    }
  }

  .current-time {
    color: #b8c5d1;
    font-size: 14px;
  }
}

.app-aside {
  background: #001529;
  border-right: 1px solid #002140;

  .sidebar-menu {
    height: 100%;
    border-right: none;
  }

  .el-menu-item {
    height: 56px;
    line-height: 56px;

    &:hover {
      background: #002140 !important;
    }

    &.is-active {
      background: #1890ff !important;
    }
  }
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
