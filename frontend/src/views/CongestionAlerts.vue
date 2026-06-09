<template>
  <div class="congestion-alerts">
    <div class="page-container mb-20">
      <div class="flex-between mb-20">
        <h2 class="page-title" style="margin: 0; border: none; padding: 0">
          <el-icon><Warning /></el-icon>
          拥堵预警展示
        </h2>
        <div class="flex-center gap-16">
          <el-button type="primary" @click="loadData">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-badge :value="parkingStore.alertCount" class="item" type="danger">
            <el-button type="danger" plain>
              <el-icon><Bell /></el-icon>
              活跃预警
            </el-button>
          </el-badge>
        </div>
      </div>

      <div class="card-container">
        <div class="stat-card">
          <div class="stat-label">活跃预警</div>
          <div class="stat-value">{{ parkingStore.alertCount }}</div>
          <div class="stat-unit">条未处理</div>
        </div>
        <div class="stat-card green">
          <div class="stat-label">今日已处理</div>
          <div class="stat-value">{{ resolvedToday }}</div>
          <div class="stat-unit">条预警</div>
        </div>
        <div class="stat-card orange">
          <div class="stat-label">高级别预警</div>
          <div class="stat-value">{{ highLevelCount }}</div>
          <div class="stat-unit">条需关注</div>
        </div>
        <div class="stat-card blue">
          <div class="stat-label">预测预警</div>
          <div class="stat-value">{{ predictionCount }}</div>
          <div class="stat-unit">条未来预警</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><Warning /></el-icon>
            <span>活跃预警列表</span>
            <div style="flex: 1"></div>
            <el-tag type="danger">{{ activeAlerts.length }} 条</el-tag>
          </div>
          <div class="alert-list">
            <div
              v-for="alert in activeAlerts"
              :key="alert.id"
              class="alert-card"
              :class="`level-${alert.alertLevel}`"
            >
              <div class="alert-header">
                <div class="flex-center gap-8">
                  <el-icon v-if="alert.alertLevel === 'high'" size="20" color="#f56c6c">
                    <CircleCloseFilled />
                  </el-icon>
                  <el-icon v-else size="20" color="#e6a23c">
                    <WarningFilled />
                  </el-icon>
                  <span class="alert-lot">{{ alert.parkingLotName }}</span>
                  <el-tag :type="alert.alertLevel === 'high' ? 'danger' : 'warning'" size="small">
                    {{ alert.alertLevel === 'high' ? '高级' : '中级' }}
                  </el-tag>
                  <el-tag :type="alert.alertType === 'real_time' ? 'danger' : 'info'" size="small">
                    {{ alert.alertType === 'real_time' ? '实时' : '预测' }}
                  </el-tag>
                </div>
              </div>
              <div class="alert-message">{{ alert.message }}</div>
              <div class="alert-info">
                <div class="flex-center gap-16">
                  <span class="info-item">
                    <el-icon><Timer /></el-icon>
                    {{ formatTime(alert.createdAt) }}
                  </span>
                  <span class="info-item" v-if="alert.occupancyRate">
                    <el-icon><Odometer /></el-icon>
                    使用率 {{ alert.occupancyRate }}%
                  </span>
                  <span class="info-item" v-if="alert.predictedTime">
                    <el-icon><Clock /></el-icon>
                    预计 {{ formatDateTime(alert.predictedTime) }}
                  </span>
                </div>
              </div>
              <div class="alert-actions">
                <el-button size="small" type="success" @click="resolveAlert(alert.id)">
                  <el-icon><Check /></el-icon>
                  标记已处理
                </el-button>
                <el-button size="small" @click="viewLotDetail(alert.parkingLotId)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
              </div>
            </div>
            <el-empty v-if="activeAlerts.length === 0" description="暂无活跃预警，所有停车场运行正常" />
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><PieChart /></el-icon>
            <span>预警类型分布</span>
          </div>
          <div ref="typeChartRef" class="chart-container" style="height: 280px"></div>
        </div>

        <div class="chart-card">
          <div class="chart-title">
            <el-icon><DataLine /></el-icon>
            <span>预警级别分布</span>
          </div>
          <div ref="levelChartRef" class="chart-container" style="height: 280px"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">
        <el-icon><List /></el-icon>
        <span>历史预警记录</span>
        <div style="flex: 1"></div>
        <el-select v-model="alertFilter" style="width: 120px" @change="filterAlerts">
          <el-option label="全部" value="all" />
          <el-option label="实时预警" value="real_time" />
          <el-option label="预测预警" value="prediction" />
          <el-option label="已处理" value="resolved" />
        </el-select>
      </div>
      <el-table :data="filteredAlerts" stripe style="width: 100%">
        <el-table-column prop="parkingLotName" label="停车场" min-width="140" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertType === 'real_time' ? 'danger' : 'info'" size="small">
              {{ row.alertType === 'real_time' ? '实时' : '预测' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.alertLevel === 'high' ? 'danger' : 'warning'" size="small">
              {{ row.alertLevel === 'high' ? '高级' : '中级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="预警内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="occupancyRate" label="使用率(%)" width="100" align="center" />
        <el-table-column label="预警时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isResolved ? 'success' : 'warning'" size="small">
              {{ row.isResolved ? '已处理' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isResolved"
              size="small"
              type="primary"
              link
              @click="resolveAlert(row.id)"
            >
              处理
            </el-button>
            <span v-else class="text-success">
              <el-icon><Check /></el-icon>
              {{ formatDateTime(row.resolvedAt) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useParkingStore } from '@/stores/parking'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecentAlerts, resolveAlert as resolveAlertApi } from '@/api/parking'
import { initChart, getPieOption } from '@/utils/chart'

const router = useRouter()
const parkingStore = useParkingStore()
const activeAlerts = ref([])
const recentAlerts = ref([])
const alertFilter = ref('all')

const typeChartRef = ref(null)
const levelChartRef = ref(null)

let typeChart = null
let levelChart = null
let refreshTimer = null

const resolvedToday = computed(() => {
  const today = new Date().toDateString()
  return recentAlerts.value.filter(a =>
    a.isResolved && new Date(a.resolvedAt).toDateString() === today
  ).length
})

const highLevelCount = computed(() => {
  return activeAlerts.value.filter(a => a.alertLevel === 'high').length
})

const predictionCount = computed(() => {
  return activeAlerts.value.filter(a => a.alertType === 'prediction').length
})

const filteredAlerts = computed(() => {
  if (alertFilter.value === 'all') return recentAlerts.value
  if (alertFilter.value === 'resolved') return recentAlerts.value.filter(a => a.isResolved)
  return recentAlerts.value.filter(a => a.alertType === alertFilter.value && !a.isResolved)
})

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const formatDateTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadData = async () => {
  try {
    await parkingStore.fetchAllData()
    activeAlerts.value = parkingStore.activeAlerts
    recentAlerts.value = await getRecentAlerts()

    nextTick(() => {
      loadTypeChart()
      loadLevelChart()
    })
  } catch (e) {
    console.error('加载预警数据失败:', e)
  }
}

const resolveAlert = async (id) => {
  try {
    await ElMessageBox.confirm('确定标记该预警为已处理？', '确认处理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await resolveAlertApi(id)
    ElMessage.success('预警已处理')
    await loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('处理预警失败:', e)
    }
  }
}

const viewLotDetail = (parkingLotId) => {
  const lot = parkingStore.parkingLots.find(l => l.id === parkingLotId)
  if (lot) {
    parkingStore.setSelectedParkingLot(lot)
    router.push('/dashboard')
  }
}

const filterAlerts = () => {
  // 已通过 computed 处理
}

const loadTypeChart = () => {
  if (!typeChartRef.value || !recentAlerts.value.length) return

  const typeCounts = {
    real_time: 0,
    prediction: 0
  }

  recentAlerts.value.forEach(a => {
    if (typeCounts[a.alertType] !== undefined) {
      typeCounts[a.alertType]++
    }
  })

  const data = [
    { name: '实时预警', value: typeCounts.real_time },
    { name: '预测预警', value: typeCounts.prediction }
  ]

  const option = getPieOption(data)

  if (typeChart) {
    typeChart.setOption(option)
  } else {
    typeChart = initChart(typeChartRef.value, option)
  }
}

const loadLevelChart = () => {
  if (!levelChartRef.value || !recentAlerts.value.length) return

  const levelCounts = {
    high: 0,
    medium: 0
  }

  recentAlerts.value.forEach(a => {
    if (levelCounts[a.alertLevel] !== undefined) {
      levelCounts[a.alertLevel]++
    }
  })

  const data = [
    { name: '高级预警', value: levelCounts.high },
    { name: '中级预警', value: levelCounts.medium }
  ]

  const option = getPieOption(data)

  if (levelChart) {
    levelChart.setOption(option)
  } else {
    levelChart = initChart(levelChartRef.value, option)
  }
}

onMounted(async () => {
  await loadData()
  refreshTimer = setInterval(loadData, 60000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (typeChart) typeChart.destroy()
  if (levelChart) levelChart.destroy()
})
</script>

<style lang="scss" scoped>
.congestion-alerts {
  .alert-list {
    max-height: 560px;
    overflow-y: auto;
  }

  .alert-card {
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 12px;
    border-left: 4px solid #e6a23c;
    background: #fdf6ec;
    transition: all 0.3s;

    &:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &.level-high {
      border-left-color: #f56c6c;
      background: #fef0f0;
    }

    .alert-header {
      margin-bottom: 8px;

      .alert-lot {
        font-weight: 600;
        font-size: 15px;
        color: #303133;
      }
    }

    .alert-message {
      color: #606266;
      line-height: 1.6;
      margin-bottom: 12px;
    }

    .alert-info {
      margin-bottom: 12px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #909399;
      }
    }

    .alert-actions {
      display: flex;
      gap: 8px;
    }
  }

  .text-success {
    color: #67c23a;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
