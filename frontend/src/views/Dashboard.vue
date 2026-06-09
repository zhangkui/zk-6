<template>
  <div class="dashboard">
    <div class="card-container">
      <div class="stat-card">
        <div class="stat-label">总停车位</div>
        <div class="stat-value">{{ parkingStore.totalSpaces.toLocaleString() }}</div>
        <div class="stat-unit">个车位</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">空闲车位</div>
        <div class="stat-value">{{ parkingStore.availableSpaces.toLocaleString() }}</div>
        <div class="stat-unit">可停放</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">平均使用率</div>
        <div class="stat-value">{{ parkingStore.avgOccupancy }}%</div>
        <div class="stat-unit">全市平均</div>
      </div>
      <div class="stat-card blue">
        <div class="stat-label">活跃预警</div>
        <div class="stat-value">{{ parkingStore.alertCount }}</div>
        <div class="stat-unit">条预警信息</div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>停车场实时状态监控</span>
            <div style="flex: 1"></div>
            <el-button size="small" type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </div>
          <el-table :data="parkingStore.parkingLots" stripe style="width: 100%" v-loading="parkingStore.loading">
            <el-table-column prop="name" label="停车场名称" min-width="140">
              <template #default="{ row }">
                <div class="flex-center gap-8">
                  <el-icon :color="getCongestionColor(row.congestionLevel)"><Location /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="district" label="区域" width="100" />
            <el-table-column prop="totalSpaces" label="总车位" width="90" align="center" />
            <el-table-column prop="availableSpaces" label="空闲" width="80" align="center" />
            <el-table-column label="使用率" width="160">
              <template #default="{ row }">
                <div class="flex-center gap-8">
                  <el-progress
                    :percentage="parseFloat(row.occupancyRate)"
                    :color="getProgressColor(row.congestionLevel)"
                    :stroke-width="12"
                  />
                  <span style="width: 50px">{{ row.occupancyRate }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :class="`congestion-tag ${row.congestionLevel}`">
                  {{ getStatusText(row.congestionLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="viewDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="chart-card" v-if="selectedLot">
          <div class="chart-title">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ selectedLot.name }} - 今日车流</span>
          </div>
          <div ref="flowChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><PieChart /></el-icon>
            <span>区域分布</span>
          </div>
          <div ref="districtChartRef" class="chart-container" style="height: 300px"></div>
        </div>

        <div class="chart-card">
          <div class="chart-title">
            <el-icon><Warning /></el-icon>
            <span>实时预警</span>
          </div>
          <div class="alert-list">
            <div
              v-for="alert in parkingStore.activeAlerts.slice(0, 5)"
              :key="alert.id"
              class="alert-item"
            >
              <div class="alert-header">
                <el-tag :type="alert.alertLevel === 'high' ? 'danger' : 'warning'" size="small">
                  {{ alert.alertType === 'real_time' ? '实时' : '预测' }}
                </el-tag>
                <span class="alert-lot">{{ alert.parkingLotName }}</span>
              </div>
              <div class="alert-message">{{ alert.message }}</div>
              <div class="alert-time">{{ formatTime(alert.createdAt) }}</div>
            </div>
            <el-empty v-if="parkingStore.activeAlerts.length === 0" description="暂无预警信息" :image-size="80" />
          </div>
        </div>

        <div class="chart-card" v-if="selectedLot">
          <div class="chart-title">
            <el-icon><Odometer /></el-icon>
            <span>{{ selectedLot.name }} - 使用率</span>
          </div>
          <div ref="gaugeChartRef" class="chart-container" style="height: 280px"></div>
        </div>
      </el-col>
    </el-row>

    <el-dialog
      v-model="detailVisible"
      :title="`${selectedLot?.name} - 车位详情`"
      width="800px"
    >
      <div v-if="selectedLot">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="停车场名称">{{ selectedLot.name }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ selectedLot.address }}</el-descriptions-item>
          <el-descriptions-item label="区域">{{ selectedLot.district }}</el-descriptions-item>
          <el-descriptions-item label="总车位">{{ selectedLot.totalSpaces }}</el-descriptions-item>
          <el-descriptions-item label="空闲车位">{{ selectedLot.availableSpaces }}</el-descriptions-item>
          <el-descriptions-item label="使用率">{{ selectedLot.occupancyRate }}%</el-descriptions-item>
        </el-descriptions>

        <div class="mb-20 mt-20">
          <h4 class="mb-16">车位状态分布（{{ spaceSummary?.totalSpaces || 0 }}个车位）</h4>
          <div class="space-summary">
            <div class="space-item occupied">
              <div class="space-count">{{ spaceSummary?.occupiedSpaces || 0 }}</div>
              <div class="space-label">已占用</div>
            </div>
            <div class="space-item available">
              <div class="space-count">{{ spaceSummary?.availableSpaces || 0 }}</div>
              <div class="space-label">空闲</div>
            </div>
            <div class="space-item rate">
              <div class="space-count">{{ spaceSummary?.occupancyRate || 0 }}%</div>
              <div class="space-label">使用率</div>
            </div>
          </div>
        </div>

        <div>
          <h4 class="mb-16">车位状态网格</h4>
          <div class="parking-grid">
            <div
              v-for="space in parkingSpaces.slice(0, 100)"
              :key="space.id"
              :class="['parking-space', space.isOccupied ? 'occupied' : 'available']"
              @click="toggleSpace(space)"
            >
              <span>{{ space.spaceNumber }}</span>
            </div>
            <div v-if="parkingSpaces.length > 100" class="more-spaces">
              还有 {{ parkingSpaces.length - 100 }} 个车位...
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useParkingStore } from '@/stores/parking'
import { ElMessage } from 'element-plus'
import {
  getHourlyFlow,
  getSpacesByParkingLot,
  getSpaceSummary,
  toggleSpaceStatus
} from '@/api/parking'
import {
  initChart,
  getLineOption,
  getPieOption,
  getGaugeOption,
  getCongestionColor,
  getHours
} from '@/utils/chart'

const parkingStore = useParkingStore()
const selectedLot = ref(null)
const detailVisible = ref(false)
const parkingSpaces = ref([])
const spaceSummary = ref(null)

const flowChartRef = ref(null)
const districtChartRef = ref(null)
const gaugeChartRef = ref(null)

let flowChart = null
let districtChart = null
let gaugeChart = null
let refreshTimer = null

const getProgressColor = (level) => {
  const colors = {
    critical: '#f56c6c',
    warning: '#e6a23c',
    moderate: '#409eff',
    normal: '#67c23a'
  }
  return colors[level] || '#909399'
}

const getStatusText = (level) => {
  const texts = {
    critical: '严重拥堵',
    warning: '拥堵',
    moderate: '中等',
    normal: '正常'
  }
  return texts[level] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const refreshData = () => {
  parkingStore.fetchAllData()
  if (selectedLot.value) {
    loadFlowChart()
    loadGaugeChart()
  }
}

const viewDetail = async (row) => {
  selectedLot.value = row
  detailVisible.value = true
  try {
    parkingSpaces.value = await getSpacesByParkingLot(row.id)
    spaceSummary.value = await getSpaceSummary(row.id)
  } catch (e) {
    console.error('加载车位详情失败:', e)
  }
}

const toggleSpace = async (space) => {
  try {
    await toggleSpaceStatus(space.id)
    space.isOccupied = !space.isOccupied
    spaceSummary.value = await getSpaceSummary(selectedLot.value.id)
    ElMessage.success('车位状态已更新')
  } catch (e) {
    console.error('更新车位状态失败:', e)
  }
}

const loadDistrictChart = () => {
  if (!districtChartRef.value) return

  const districtMap = {}
  parkingStore.parkingLots.forEach(lot => {
    if (!districtMap[lot.district]) {
      districtMap[lot.district] = 0
    }
    districtMap[lot.district]++
  })

  const data = Object.entries(districtMap).map(([name, value]) => ({ name, value }))
  const option = getPieOption(data, '')

  if (districtChart) {
    districtChart.setOption(option)
  } else {
    districtChart = initChart(districtChartRef.value, option)
  }
}

const loadFlowChart = () => {
  if (!flowChartRef.value || !selectedLot.value) return

  getHourlyFlow(selectedLot.value.id).then(data => {
    const xData = getHours()
    const inflowData = new Array(24).fill(0)
    const outflowData = new Array(24).fill(0)
    const occupancyData = new Array(24).fill(0)

    data.forEach(item => {
      inflowData[item.hour] = item.inflow
      outflowData[item.hour] = item.outflow
      occupancyData[item.hour] = parseFloat(item.occupancyRate)
    })

    const option = getLineOption(xData, [
      { name: '驶入', data: inflowData, area: true },
      { name: '驶出', data: outflowData, area: true },
      { name: '使用率(%)', data: occupancyData, area: false }
    ], '')

    if (flowChart) {
      flowChart.setOption(option)
    } else {
      flowChart = initChart(flowChartRef.value, option)
    }
  }).catch(e => {
    console.error('加载车流数据失败:', e)
  })
}

const loadGaugeChart = () => {
  if (!gaugeChartRef.value || !selectedLot.value) return

  const rate = parseFloat(selectedLot.value.occupancyRate)
  let color = '#67c23a'
  if (rate >= 90) color = '#f56c6c'
  else if (rate >= 75) color = '#e6a23c'
  else if (rate >= 50) color = '#409eff'

  const option = getGaugeOption(rate, '', color)

  if (gaugeChart) {
    gaugeChart.setOption(option)
  } else {
    gaugeChart = initChart(gaugeChartRef.value, option)
  }
}

watch(() => parkingStore.parkingLots, () => {
  nextTick(() => {
    loadDistrictChart()
  })
}, { deep: true })

watch(selectedLot, () => {
  nextTick(() => {
    if (selectedLot.value) {
      loadFlowChart()
      loadGaugeChart()
    }
  })
})

onMounted(async () => {
  await parkingStore.fetchAllData()

  if (parkingStore.parkingLots.length > 0) {
    selectedLot.value = parkingStore.parkingLots[0]
  }

  nextTick(() => {
    loadDistrictChart()
    if (selectedLot.value) {
      loadFlowChart()
      loadGaugeChart()
    }
  })

  refreshTimer = setInterval(() => {
    parkingStore.fetchParkingStatus()
    if (selectedLot.value) {
      loadGaugeChart()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (flowChart) flowChart.destroy()
  if (districtChart) districtChart.destroy()
  if (gaugeChart) gaugeChart.destroy()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .alert-list {
    max-height: 320px;
    overflow-y: auto;
  }

  .alert-item {
    padding: 12px;
    border-radius: 8px;
    background: #f5f7fa;
    margin-bottom: 10px;
    transition: all 0.3s;

    &:hover {
      background: #ebeef5;
    }

    .alert-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .alert-lot {
        font-weight: 600;
        color: #303133;
      }
    }

    .alert-message {
      font-size: 13px;
      color: #606266;
      margin-bottom: 4px;
      line-height: 1.5;
    }

    .alert-time {
      font-size: 12px;
      color: #909399;
    }
  }

  .space-summary {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .space-item {
      flex: 1;
      padding: 20px;
      border-radius: 8px;
      text-align: center;

      &.occupied {
        background: #fef0f0;

        .space-count {
          color: #f56c6c;
        }
      }

      &.available {
        background: #f0f9eb;

        .space-count {
          color: #67c23a;
        }
      }

      &.rate {
        background: #ecf5ff;

        .space-count {
          color: #409eff;
        }
      }

      .space-count {
        font-size: 28px;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .space-label {
        font-size: 13px;
        color: #606266;
      }
    }
  }

  .parking-grid {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 6px;

    .parking-space {
      aspect-ratio: 2;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid transparent;

      &.occupied {
        background: #fef0f0;
        color: #f56c6c;
        border-color: #fde2e2;
      }

      &.available {
        background: #f0f9eb;
        color: #67c23a;
        border-color: #e1f3d8;
      }

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }
    }

    .more-spaces {
      grid-column: span 10;
      text-align: center;
      padding: 12px;
      color: #909399;
      font-size: 13px;
    }
  }
}
</style>
