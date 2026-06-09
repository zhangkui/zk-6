<template>
  <div class="traffic-analysis">
    <div class="page-container mb-20">
      <div class="flex-between mb-20">
        <h2 class="page-title" style="margin: 0; border: none; padding: 0">
          <el-icon><TrendCharts /></el-icon>
          历史车流分析
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
          <el-select v-model="analysisDays" style="width: 120px" @change="loadData">
            <el-option label="近7天" :value="7" />
            <el-option label="近15天" :value="15" />
            <el-option label="近30天" :value="30" />
          </el-select>
        </div>
      </div>

      <div class="card-container" v-if="analysisData">
        <div class="stat-card">
          <div class="stat-label">总驶入量</div>
          <div class="stat-value">{{ analysisData.totalInflow?.toLocaleString() || 0 }}</div>
          <div class="stat-unit">辆次</div>
        </div>
        <div class="stat-card green">
          <div class="stat-label">总驶出量</div>
          <div class="stat-value">{{ analysisData.totalOutflow?.toLocaleString() || 0 }}</div>
          <div class="stat-unit">辆次</div>
        </div>
        <div class="stat-card orange">
          <div class="stat-label">平均使用率</div>
          <div class="stat-value">{{ analysisData.avgOccupancyRate || 0 }}%</div>
          <div class="stat-unit">{{ analysisDays }}天平均</div>
        </div>
        <div class="stat-card blue">
          <div class="stat-label">高峰时段</div>
          <div class="stat-value">{{ formatHour(analysisData.peakHour?.hour) }}</div>
          <div class="stat-unit">使用率 {{ analysisData.peakHour?.occupancyRate || 0 }}%</div>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><Histogram /></el-icon>
            <span>日均时段车流分布</span>
          </div>
          <div ref="hourlyChartRef" class="chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><TrendCharts /></el-icon>
            <span>每日车流趋势</span>
          </div>
          <div ref="dailyChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">
        <el-icon><Calendar /></el-icon>
        <span>周分析对比</span>
      </div>
      <div ref="weekdayChartRef" class="chart-container" style="height: 420px"></div>
    </div>

    <el-row :gutter="20" v-if="analysisData?.weekdayAnalysis">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">
            <el-icon><DataLine /></el-icon>
            <span>各工作日时段对比</span>
          </div>
          <el-tabs v-model="activeWeekdayTab" type="card">
            <el-tab-pane
              v-for="(day, index) in analysisData.weekdayAnalysis"
              :key="index"
              :label="day.weekday"
              :name="String(index)"
            >
              <div :ref="el => setWeekdayChartRef(el, index)" class="chart-container"></div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useParkingStore } from '@/stores/parking'
import { getTrafficAnalysis, getDailyFlow, getAvgHourlyFlow } from '@/api/parking'
import {
  initChart,
  getLineOption,
  getBarOption,
  getHours,
  formatDate
} from '@/utils/chart'

const parkingStore = useParkingStore()
const selectedLotId = ref(null)
const analysisDays = ref(30)
const analysisData = ref(null)
const activeWeekdayTab = ref('0')

const hourlyChartRef = ref(null)
const dailyChartRef = ref(null)
const weekdayChartRef = ref(null)
const weekdayChartRefs = ref([])

let hourlyChart = null
let dailyChart = null
let weekdayChart = null
let weekdayCharts = []

const setWeekdayChartRef = (el, index) => {
  if (el) {
    weekdayChartRefs.value[index] = el
  }
}

const formatHour = (hour) => {
  if (hour === null || hour === undefined) return '--'
  return `${String(hour).padStart(2, '0')}:00`
}

const loadData = async () => {
  if (!selectedLotId.value) return

  try {
    analysisData.value = await getTrafficAnalysis(selectedLotId.value, analysisDays.value)
    await nextTick()
    loadHourlyChart()
    loadDailyChart()
    loadWeekdayChart()
    loadWeekdayTabsCharts()
  } catch (e) {
    console.error('加载车流分析数据失败:', e)
  }
}

const loadHourlyChart = () => {
  if (!hourlyChartRef.value || !analysisData.value?.avgHourly) return

  const xData = getHours()
  const inflowData = new Array(24).fill(0)
  const outflowData = new Array(24).fill(0)
  const occupancyData = new Array(24).fill(0)

  analysisData.value.avgHourly.forEach(item => {
    inflowData[item.hour] = Math.round(item.inflow)
    outflowData[item.hour] = Math.round(item.outflow)
    occupancyData[item.hour] = parseFloat(item.occupancyRate)
  })

  const option = getBarOption(xData, [
    { name: '平均驶入', data: inflowData },
    { name: '平均驶出', data: outflowData }
  ])

  if (hourlyChart) {
    hourlyChart.setOption(option)
  } else {
    hourlyChart = initChart(hourlyChartRef.value, option)
  }
}

const loadDailyChart = () => {
  if (!dailyChartRef.value || !analysisData.value?.dailyFlows) return

  const xData = analysisData.value.dailyFlows.map(item => {
    const d = new Date(item.date)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  const inflowData = analysisData.value.dailyFlows.map(item => item.totalInflow)
  const outflowData = analysisData.value.dailyFlows.map(item => item.totalOutflow)

  const option = getLineOption(xData, [
    { name: '驶入', data: inflowData, area: true },
    { name: '驶出', data: outflowData, area: true }
  ])

  if (dailyChart) {
    dailyChart.setOption(option)
  } else {
    dailyChart = initChart(dailyChartRef.value, option)
  }
}

const loadWeekdayChart = () => {
  if (!weekdayChartRef.value || !analysisData.value?.weekdayAnalysis) return

  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const xData = weekdays
  const avgOccupancyData = analysisData.value.weekdayAnalysis.map(day =>
    parseFloat(day.avgOccupancy) || 0
  )
  const peakOccupancyData = analysisData.value.weekdayAnalysis.map(day =>
    day.peakHour ? parseFloat(day.peakHour.occupancyRate) : 0
  )

  const option = getBarOption(xData, [
    { name: '平均使用率(%)', data: avgOccupancyData },
    { name: '峰值使用率(%)', data: peakOccupancyData }
  ])

  if (weekdayChart) {
    weekdayChart.setOption(option)
  } else {
    weekdayChart = initChart(weekdayChartRef.value, option)
  }
}

const loadWeekdayTabsCharts = () => {
  if (!analysisData.value?.weekdayAnalysis) return

  nextTick(() => {
    weekdayCharts.forEach(chart => chart?.destroy())
    weekdayCharts = []

    analysisData.value.weekdayAnalysis.forEach((day, index) => {
      const chartEl = weekdayChartRefs.value[index]
      if (!chartEl || !day.hourlyData) return

      const xData = getHours()
      const occupancyData = new Array(24).fill(0)
      const inflowData = new Array(24).fill(0)

      day.hourlyData.forEach(item => {
        occupancyData[item.hour] = parseFloat(item.occupancyRate) || 0
        inflowData[item.hour] = Math.round(item.inflow) || 0
      })

      const option = getLineOption(xData, [
        { name: '使用率(%)', data: occupancyData, area: true },
        { name: '驶入量', data: inflowData, area: false }
      ])

      const chart = initChart(chartEl, option)
      weekdayCharts.push(chart)
    })
  })
}

watch(activeWeekdayTab, () => {
  nextTick(() => {
    loadWeekdayTabsCharts()
  })
})

onMounted(async () => {
  await parkingStore.fetchParkingStatus()
  if (parkingStore.parkingLots.length > 0) {
    selectedLotId.value = parkingStore.parkingLots[0].id
    await loadData()
  }
})

onUnmounted(() => {
  if (hourlyChart) hourlyChart.destroy()
  if (dailyChart) dailyChart.destroy()
  if (weekdayChart) weekdayChart.destroy()
  weekdayCharts.forEach(chart => chart?.destroy())
})
</script>

<style lang="scss" scoped>
.traffic-analysis {
  .el-tabs {
    margin-top: 16px;

    :deep(.el-tab-pane) {
      min-height: 360px;
    }
  }
}
</style>
