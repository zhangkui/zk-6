<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="28" color="#409eff"><Location /></el-icon>
        <span>停车热度平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#b0bec5"
        active-text-color="#409eff"
        class="menu">
        <el-menu-item index="/monitor">
          <el-icon><Monitor /></el-icon>
          <span>余位监测</span>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><DataAnalysis /></el-icon>
          <span>历史车流分析</span>
        </el-menu-item>
        <el-menu-item index="/prediction">
          <el-icon><TrendCharts /></el-icon>
          <span>热度预测</span>
        </el-menu-item>
        <el-menu-item index="/warning">
          <el-icon><Warning /></el-icon>
          <span>拥堵预警</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">城市停车热度预测与可视化平台</div>
        <div class="header-right">
          <el-tag type="success" effect="light">系统运行正常</el-tag>
          <span class="current-time">{{ currentTime }}</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
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

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 24px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-time {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
