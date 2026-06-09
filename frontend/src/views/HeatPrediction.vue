<template>
  <div class="heat-prediction">
    <div class="page-container mb-20">
      <div class="flex-between mb-20">
        <h2 class="page-title" style="margin: 0; border: none; padding: 0">
          <el-icon><Histogram /></el-icon>
          分时段热度预测
        </h2>
        <div class="flex-center gap-16">
          <el-select v-model="selectedLotId" placeholder="选择停车场" style="width: 200px" @change="loadData">
            <el-option
              v-for="lot in parkingStore.parkingLots"
              :key="lot.id"
              :label="lot.name"
              :value="lot.id"
            />
          </el-select>
          <el-select v-model="predictionDays" style="width: 120px" @change="loadData">
            <el-option label="未来1天" :value="1" />
            <el-option label="未来3天" :value="3" />
            <el-option label="未来7天" :value="7" />
          </el-select>
          <el-button type="primary" @click="generatePrediction">
            <el-icon><RefreshRight /></el-icon>
            重新预测
          </el-button>
        </div>
      </div>

      <div class="card-container" v-if="predictionSummary">
        <div class="stat-card">
          <div class="stat-label">预测天数</div>
          <div class="stat-value">{{ predictionSummary.totalDays }}</div>
          <div class="stat-unit">天预测数据</div>
        </div>
        <div class="stat-card green">
          <div class="stat-label">高热度时段</div>
          <div class="stat-value">{{ totalHighHeatHours }}</div>
          <div class="stat-unit">小时</div>
        </div>
        <div class="stat-card orange">
          <div class="stat-label">预测峰值</div>
          <div class="stat-value">{{ maxPredictedRate }}%</div>
          <div class="stat-unit">最高使用率</div>
        </div>
        <div class="stat-card blue">
          <div class="stat-label">平均置信度</div>
          <div class="stat-value">{{ avgConfidence }}%</div>
          <div class="stat-unit">模型置信度</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><DataLine /></el-icon>
            <span>热度预测热力图</span>
          </div>
          <div ref="heatmapChartRef" class="chart-container" style="height: 420px"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><PieChart /></el-icon>
            <span>热度等级分布</span>
          </div>
          <div ref="pieChartRef" class="chart-container" style="height: 420px"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">
        <el-icon><TrendCharts /></el-icon>
        <span>预测趋势对比</span>
      </div>
      <div ref="trendChartRef" class="chart-container"></div>
    </div>

    <div class="chart-card">
      <div class="chart-title">
        <el-icon><List /></el-icon>
        <span>每日预测详情</span>
      </div>
      <el-collapse>
        <el-collapse-item
          v-for="(day, index) in predictionSummary?.dailySummaries || []"
          :key="index"
          :name="String(index)"
        >
          <template #title>
            <div class="flex-between" style="width: 100%; padding-right: 20px">
              <div class="flex-center gap-8">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(day.date) }}</span>
              </div>
              <div class="flex-center gap-16">
                <el-tag type="info">平均使用率: {{ day.avgOccupancy }}%</el-tag>
                <el-tag type="warning">高热度: {{ day.highHeatHours }}小时</el-tag>
                <el-tag type="danger" v-if="day.highHeatHours > 4">需关注</el-tag>
              </div>
            </div>
          </template>
          <div :ref="el => setDailyChartRef(el, index)" class="chart-container" style="height: 300px"></div>
          <div class="heat-level-legend mt-16">
            <span class="heat-tag extreme">极高 ≥90%</span>
            <span class="heat-tag high">高 ≥75%</span>
            <span class="heat-tag medium">中 ≥50%</span>
            <span class="heat-tag low">低 ≥25%</span>
            <span class="heat-tag idle">空闲 <25%</span>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useParkingStore } from '@/stores/parking'
import { ElMessage } from 'element-plus'
import {
  getPredictionSummary,
  getPredictionRange,
  getHeatMapData
} from '@/api/parking'
import {
  initChart,
  getLineOption,
  getPieOption,
  getHeatmapOption,
  getHours,
  getHeatLevelColor,
  formatDate as formatDateUtil
} from '@/utils/chart'

const parkingStore = useParkingStore()
const selectedLotId = ref(null)
const predictionDays = ref(7)
const predictionSummary = ref(null)
const heatMapData = ref([])
const predictionData = ref([])

const heatmapChartRef = ref(null)
const pieChartRef = ref(null)
const trendChartRef = ref(null)
const dailyChartRefs = ref([])

let heatmapChart = null
let pieChart = null
let trendChart = null
let dailyCharts = []

const setDailyChartRef = (el, index) => {
  if (el) {
    dailyChartRefs.value[index] = el
  }
}

const formatDate = (date) => {
  return formatDateUtil(date)
}

const totalHighHeatHours = computed(() => {
  if (!predictionSummary.value?.dailySummaries) return 0
  return predictionSummary.value.dailySummaries.reduce((sum, day) => sum + (day.highHeatHours || 0), 0)
})

const maxPredictedRate = computed(() => {
  if (!predictionData.value.length) return 0
  const max = Math.max(...predictionData.value.map(p => parseFloat(p.predictedOccupancyRate)))
  return max.toFixed(1)
})

const avgConfidence = computed(() => {
  if (!predictionData.value.length) return 0
  const avg = predictionData.value.reduce((sum, p) => sum + parseFloat(p.confidence || 0), 0) / predictionData.value.length
  return avg.toFixed(1)
})

const loadData = async () => {
  if (!selectedLotId.value) return

  try {
    const [summary, data, heatmap] = await Promise.all([
      getPredictionSummary(selectedLotId.value, predictionDays.value),
      getPredictionRange(selectedLotId.value, predictionDays.value),
      getHeatMapData(selectedLotId.value, predictionDays.value)
    ])

    predictionSummary.value = summary
    predictionData.value = data
    heatMapData.value = heatmap

    await nextTick()
    loadHeatmapChart()
    loadPieChart()
    loadTrendChart()
    loadDailyCharts()
  } catch (e) {
    console.error('加载预测数据失败:', e)
  }
}

const generatePrediction = async () => {
  if (!selectedLotId.value) return
  ElMessage.info('正在重新生成预测数据...')
  await loadData()
  ElMessage.success('预测数据已更新')
}

const loadHeatmapChart = () => {
  if (!heatmapChartRef.value || !heatMapData.value.length) return

  const dateSet = new Set(heatMapData.value.map(d => d.date))
  const yData = Array.from(dateSet).sort()
  const xData = getHours()

  const data = []
  heatMapData.value.forEach(d => {
    const xIndex = xData.indexOf(`${String(d.hour).padStart(2, '0')}:00`)
    const yIndex = yData.indexOf(d.date)
    if (xIndex >= 0 && yIndex >= 0) {
      data.push([xIndex, yIndex, parseFloat(d.occupancy)])
    }
  })

  const yLabels = yData.map(d => {
    const date = new Date(d)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })

  const option = getHeatmapOption(xData, yLabels, data)

  if (heatmapChart) {
    heatmapChart.setOption(option)
  } else {
    heatmapChart = initChart(heatmapChartRef.value, option)
  }
}

const loadPieChart = () => {
  if (!pieChartRef.value || !predictionData.value.length) return

  const levelCounts = {
    extreme: 0,
    high: 0,
    medium: 0,
    low: 0,
    idle: 0
  }

  predictionData.value.forEach(p => {
    const level = p.heatLevel || 'idle'
    if (levelCounts[level] !== undefined) {
      levelCounts[level]++
    }
  })

  const levelNames = {
    extreme: '极高热度',
    high: '高热度',
    medium: '中等热度',
    low: '低热度',
    idle: '空闲'
  }

  const data = Object.entries(levelCounts)
    .filter(([, value]) => value > 0)
    .map(([key, value]) => ({
      name: levelNames[key] || key,
      value
    }))

  const option = getPieOption(data)

  if (pieChart) {
    pieChart.setOption(option)
  } else {
    pieChart = initChart(pieChartRef.value, option)
  }
}

const loadTrendChart = () => {
  if (!trendChartRef.value || !predictionSummary.value?.dailySummaries) return

  const xData = predictionSummary.value.dailySummaries.map(day => formatDate(day.date))
  const avgData = predictionSummary.value.dailySummaries.map(day => parseFloat(day.avgOccupancy) || 0)
  const peakData = predictionSummary.value.dailySummaries.map(day =>
    day.peakPrediction ? parseFloat(day.peakPrediction.predictedOccupancyRate) : 0
  )

  const option = getLineOption(xData, [
    { name: '平均预测使用率(%)', data: avgData, area: true },
    { name: '峰值预测使用率(%)', data: peakData, area: false }
  ])

  if (trendChart) {
    trendChart.setOption(option)
  } else {
    trendChart = initChart(trendChartRef.value, option)
  }
}

const loadDailyCharts = () => {
  if (!predictionSummary.value?.dailySummaries) return

  dailyCharts.forEach(chart => chart?.destroy())
  dailyCharts = []

  nextTick(() => {
    predictionSummary.value.dailySummaries.forEach((day, index) => {
      const chartEl = dailyChartRefs.value[index]
      if (!chartEl || !day.hourlyPredictions) return

      const xData = getHours()
      const occupancyData = new Array(24).fill(0)
      const inflowData = new Array(24).fill(0)

      day.hourlyPredictions.forEach(item => {
        occupancyData[item.hour] = parseFloat(item.predictedOccupancyRate) || 0
        inflowData[item.hour] = item.predictedInflow || 0
      })

      const option = getLineOption(xData, [
        { name: '预测使用率(%)', data: occupancyData, area: true },
        { name: '预测驶入量', data: inflowData, area: false }
      ])

      const chart = initChart(chartEl, option)
      dailyCharts.push(chart)
    })
  })
}

onMounted(async () => {
  await parkingStore.fetchParkingStatus()
  if (parkingStore.parkingLots.length > 0) {
    selectedLotId.value = parkingStore.parkingLots[0].id
    await loadData()
  }
})

onUnmounted(() => {
  if (heatmapChart) heatmapChart.destroy()
  if (pieChart) pieChart.destroy()
  if (trendChart) trendChart.destroy()
  dailyCharts.forEach(chart => chart?.destroy())
})
</script>

<style lang="scss" scoped>
.heat-prediction {
  .el-collapse {
    border: none;

    :deep(.el-collapse-item__header) {
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 8px;
      padding: 0 16px;
    }

    :deep(.el-collapse-item__wrap) {
      border: 1px solid #ebeef5;
      border-radius: 0 0 8px 8px;
      margin-bottom: 8px;
    }
  }

  .heat-level-legend {
    display: flex;
    gap: 12px;
    justify-content: center;
    padding: 12px;
    background: #fafafa;
    border-radius: 8px;

    .heat-tag {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 12px;
      color: #fff;

      &.extreme {
        background: #f5222d;
      }
      &.high {
        background: #fa8c16;
      }
      &.medium {
        background: #faad14;
      }
      &.low {
        background: #52c41a;
      }
      &.idle {
        background: #8c8c8c;
      }
    }
  }
}
</style>
