<template>
  <div class="page-container">
    <div class="page-header">
      <h2>历史车流分析</h2>
      <p>分析停车场历史车流数据，洞察停车需求变化规律</p>
    </div>

    <div class="chart-card">
      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="选择停车场">
          <el-select v-model="selectedLotId" placeholder="请选择停车场" style="width: 240px;">
            <el-option
              v-for="lot in parkingLots"
              :key="lot.id"
              :label="lot.name"
              :value="lot.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAnalysisData">
            <el-icon><Search /></el-icon>查询分析
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">日均时段占用率趋势</div>
          <div ref="occupancyChartRef" style="width: 100%; height: 360px;"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">出入车流对比（按小时平均）</div>
          <div ref="flowChartRef" style="width: 100%; height: 340px;"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">每日车流总量趋势</div>
          <div ref="dailyChartRef" style="width: 100%; height: 340px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">分析统计摘要</div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value" style="color: #f56c6c;">{{ summary.peakHour || '-' }}</div>
            <div class="stat-label">最高峰时段</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value" style="color: #e6a23c;">{{ summary.avgOccupancy || 0 }}%</div>
            <div class="stat-label">平均占用率</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value" style="color: #409eff;">{{ summary.totalEntries || 0 }}</div>
            <div class="stat-label">总进入车辆</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value" style="color: #67c23a;">{{ summary.totalExits || 0 }}</div>
            <div class="stat-label">总离开车辆</div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getAllParkingLots } from '../api/parkingLot'
import { getHourlyAverageStats, getDailyStats } from '../api/traffic'

const parkingLots = ref([])
const selectedLotId = ref(null)
const dateRange = ref([])
const occupancyChartRef = ref(null)
const flowChartRef = ref(null)
const dailyChartRef = ref(null)

let occupancyChart = null
let flowChart = null
let dailyChart = null

const summary = ref({})

const initDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 14)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [fmt(start), fmt(end)]
}

const loadParkingLots = async () => {
  const res = await getAllParkingLots()
  parkingLots.value = res.data || []
  if (parkingLots.value.length > 0) {
    selectedLotId.value = parkingLots.value[0].id
  }
}

const loadAnalysisData = async () => {
  if (!selectedLotId.value || !dateRange.value || dateRange.value.length < 2) {
    return
  }
  const [startDate, endDate] = dateRange.value

  try {
    const [hourlyRes, dailyRes] = await Promise.all([
      getHourlyAverageStats(selectedLotId.value, startDate, endDate),
      getDailyStats(selectedLotId.value, startDate, endDate)
    ])

    const hourlyData = hourlyRes.data || {}
    const dailyData = dailyRes.data || {}

    await nextTick()
    renderOccupancyChart(hourlyData)
    renderFlowChart(hourlyData)
    renderDailyChart(dailyData)

    computeSummary(hourlyData, dailyData)
  } catch (e) {
    console.error('加载分析数据失败', e)
  }
}

const computeSummary = (hourly, daily) => {
  const avgOccList = hourly.avgOccupancy || []
  const hours = hourly.hours || []
  let maxIdx = 0
  avgOccList.forEach((v, i) => {
    if (v > (avgOccList[maxIdx] || 0)) maxIdx = i
  })
  const peakHour = hours.length > 0 ? `${hours[maxIdx]}:00-${hours[maxIdx] + 1}:00` : '-'
  const avgOcc = avgOccList.length
    ? (avgOccList.reduce((a, b) => a + Number(b), 0) / avgOccList.length).toFixed(2)
    : 0
  const totalEntries = (daily.totalEntries || []).reduce((a, b) => a + b, 0)
  const totalExits = (daily.totalExits || []).reduce((a, b) => a + b, 0)

  summary.value = { peakHour, avgOccupancy: avgOcc, totalEntries, totalExits }
}

const renderOccupancyChart = (data) => {
  if (!occupancyChartRef.value) return
  if (!occupancyChart) occupancyChart = echarts.init(occupancyChartRef.value)
  const hours = (data.hours || []).map(h => `${h}:00`)

  occupancyChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: hours },
    yAxis: { type: 'value', name: '占用率 (%)', max: 100 },
    series: [{
      name: '平均占用率',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.5)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ])
      },
      lineStyle: { color: '#409eff', width: 3 },
      itemStyle: { color: '#409eff' },
      data: data.avgOccupancy || []
    }]
  })
}

const renderFlowChart = (data) => {
  if (!flowChartRef.value) return
  if (!flowChart) flowChart = echarts.init(flowChartRef.value)
  const hours = (data.hours || []).map(h => `${h}:00`)

  flowChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['进入车辆', '离开车辆'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hours, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '车辆数' },
    series: [
      { name: '进入车辆', type: 'bar', data: data.totalEntries || [], itemStyle: { color: '#67c23a' } },
      { name: '离开车辆', type: 'bar', data: data.totalExits || [], itemStyle: { color: '#e6a23c' } }
    ]
  })
}

const renderDailyChart = (data) => {
  if (!dailyChartRef.value) return
  if (!dailyChart) dailyChart = echarts.init(dailyChartRef.value)

  dailyChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['进入车辆', '离开车辆', '平均占用率'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.dates || [] },
    yAxis: [
      { type: 'value', name: '车辆数' },
      { type: 'value', name: '占用率 (%)', max: 100 }
    ],
    series: [
      { name: '进入车辆', type: 'bar', data: data.totalEntries || [], itemStyle: { color: '#67c23a' } },
      { name: '离开车辆', type: 'bar', data: data.totalExits || [], itemStyle: { color: '#909399' } },
      {
        name: '平均占用率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: data.avgOccupancy || [],
        lineStyle: { color: '#f56c6c', width: 2 },
        itemStyle: { color: '#f56c6c' }
      }
    ]
  })
}

const handleResize = () => {
  occupancyChart?.resize()
  flowChart?.resize()
  dailyChart?.resize()
}

onMounted(async () => {
  initDateRange()
  await loadParkingLots()
  await nextTick()
  loadAnalysisData()
  window.addEventListener('resize', handleResize)
})
</script>
