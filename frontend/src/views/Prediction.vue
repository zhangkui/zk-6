<template>
  <div class="page-container">
    <div class="page-header">
      <h2>分时段热度预测</h2>
      <p>基于历史数据智能预测各时段停车热度，提前规划出行方案</p>
    </div>

    <div class="chart-card">
      <el-form :inline="true">
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
        <el-form-item label="预测日期">
          <el-date-picker
            v-model="predictDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPredictionData">
            <el-icon><View /></el-icon>查看预测
          </el-button>
          <el-button @click="regeneratePredictions">
            <el-icon><Refresh /></el-icon>重新生成预测
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-title">24小时热度预测曲线</div>
          <div ref="predictionChartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-title">热度等级分布</div>
          <div ref="heatLevelChartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #c0392b;">{{ peakInfo.hour || '-' }}</div>
          <div class="stat-label">预测最高峰时段</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #f56c6c;">{{ peakInfo.occupancy || 0 }}%</div>
          <div class="stat-label">峰值占用率预测</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #67c23a;">{{ peakInfo.avgConfidence || 0 }}%</div>
          <div class="stat-label">平均预测置信度</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #409eff;">{{ peakInfo.extremeHours || 0 }}h</div>
          <div class="stat-label">极高热度时长</div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">时段预测详情</div>
      <el-table :data="predictionData" stripe border style="width: 100%" max-height="380">
        <el-table-column prop="predictHour" label="时段" width="100" align="center">
          <template #default="{ row }">
            <span style="font-weight: 600;">{{ row.predictHour }}:00 - {{ row.predictHour + 1 }}:00</span>
          </template>
        </el-table-column>
        <el-table-column prop="predictedOccupancy" label="预测占用率" width="180">
          <template #default="{ row }">
            <el-progress
              :percentage="Number(row.predictedOccupancy)"
              :color="getHeatColor(row.heatLevel)"
              :stroke-width="14"
            />
          </template>
        </el-table-column>
        <el-table-column label="热度等级" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getHeatTagType(row.heatLevel)" effect="light">
              {{ getHeatLabel(row.heatLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="140" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600;">{{ row.confidence }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="出行建议" min-width="200">
          <template #default="{ row }">
            <span :style="{ color: getHeatColor(row.heatLevel) }">
              {{ getAdvice(row.heatLevel) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getAllParkingLots } from '../api/parkingLot'
import { getPredictionsByLotAndDate, generatePredictions } from '../api/prediction'

const parkingLots = ref([])
const selectedLotId = ref(null)
const predictDate = ref('')
const predictionData = ref([])
const peakInfo = ref({})
const predictionChartRef = ref(null)
const heatLevelChartRef = ref(null)

let predictionChart = null
let heatLevelChart = null

const getHeatColor = (level) => {
  switch (level) {
    case 'EXTREME': return '#c0392b'
    case 'HIGH': return '#f56c6c'
    case 'MODERATE': return '#e6a23c'
    default: return '#67c23a'
  }
}

const getHeatTagType = (level) => {
  switch (level) {
    case 'EXTREME': return 'danger'
    case 'HIGH': return 'warning'
    case 'MODERATE': return 'warning'
    default: return 'success'
  }
}

const getHeatLabel = (level) => {
  switch (level) {
    case 'EXTREME': return '极高热度'
    case 'HIGH': return '高热度'
    case 'MODERATE': return '中等热度'
    default: return '低热度'
  }
}

const getAdvice = (level) => {
  switch (level) {
    case 'EXTREME': return '建议避开此时段或选择周边停车场'
    case 'HIGH': return '建议提前出发，预留找车位时间'
    case 'MODERATE': return '停车难度适中，正常出行即可'
    default: return '车位充足，推荐此时段前往'
  }
}

const initDate = () => {
  const d = new Date()
  predictDate.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const loadParkingLots = async () => {
  const res = await getAllParkingLots()
  parkingLots.value = res.data || []
  if (parkingLots.value.length > 0) {
    selectedLotId.value = parkingLots.value[0].id
  }
}

const loadPredictionData = async () => {
  if (!selectedLotId.value || !predictDate.value) return
  try {
    const res = await getPredictionsByLotAndDate(selectedLotId.value, predictDate.value)
    predictionData.value = res.data || []
    await nextTick()
    renderPredictionChart()
    renderHeatLevelChart()
    computePeakInfo()
  } catch (e) {
    console.error(e)
  }
}

const regeneratePredictions = async () => {
  try {
    await generatePredictions(7)
    ElMessage.success('预测数据重新生成成功')
    loadPredictionData()
  } catch (e) {
    console.error(e)
  }
}

const computePeakInfo = () => {
  if (!predictionData.value.length) {
    peakInfo.value = {}
    return
  }
  let max = predictionData.value[0]
  predictionData.value.forEach(p => {
    if (Number(p.predictedOccupancy) > Number(max.predictedOccupancy)) max = p
  })
  const extremeHours = predictionData.value.filter(p => p.heatLevel === 'EXTREME').length
  const avgConfidence = predictionData.value.length
    ? (predictionData.value.reduce((s, p) => s + Number(p.confidence || 0), 0) / predictionData.value.length).toFixed(1)
    : 0

  peakInfo.value = {
    hour: `${max.predictHour}:00-${max.predictHour + 1}:00`,
    occupancy: max.predictedOccupancy,
    avgConfidence,
    extremeHours
  }
}

const renderPredictionChart = () => {
  if (!predictionChartRef.value) return
  if (!predictionChart) predictionChart = echarts.init(predictionChartRef.value)

  const hours = predictionData.value.map(p => `${p.predictHour}:00`)
  const values = predictionData.value.map(p => Number(p.predictedOccupancy))
  const levels = predictionData.value.map(p => p.heatLevel)

  const visualPieces = [
    { lte: 39, color: '#67c23a' },
    { gt: 39, lte: 64, color: '#e6a23c' },
    { gt: 64, lte: 84, color: '#f56c6c' },
    { gt: 84, color: '#c0392b' }
  ]

  predictionChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const idx = params[0].dataIndex
        const p = predictionData.value[idx]
        return `${params[0].name}<br/>预测占用率: ${p.predictedOccupancy}%<br/>热度: ${getHeatLabel(p.heatLevel)}<br/>置信度: ${p.confidence}%`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '占用率 (%)', max: 100 },
    visualMap: {
      show: false,
      pieces: visualPieces,
      outOfRange: { color: '#999' }
    },
    series: [{
      name: '预测占用率',
      type: 'line',
      smooth: true,
      data: values,
      lineStyle: { width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245,108,108,0.4)' },
          { offset: 1, color: 'rgba(103,194,58,0.1)' }
        ])
      },
      markLine: {
        silent: true,
        lineStyle: { color: '#333' },
        data: [
          { yAxis: 85, label: { formatter: '极高热度' }, lineStyle: { color: '#c0392b' } },
          { yAxis: 65, label: { formatter: '高热度' }, lineStyle: { color: '#f56c6c' } },
          { yAxis: 40, label: { formatter: '中等热度' }, lineStyle: { color: '#e6a23c' } }
        ]
      }
    }]
  })
}

const renderHeatLevelChart = () => {
  if (!heatLevelChartRef.value) return
  if (!heatLevelChart) heatLevelChart = echarts.init(heatLevelChartRef.value)

  const counts = { LOW: 0, MODERATE: 0, HIGH: 0, EXTREME: 0 }
  predictionData.value.forEach(p => {
    if (counts[p.heatLevel] !== undefined) counts[p.heatLevel]++
  })

  heatLevelChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}小时 ({d}%)' },
    legend: { orient: 'horizontal', bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: [
        { value: counts.LOW, name: '低热度', itemStyle: { color: '#67c23a' } },
        { value: counts.MODERATE, name: '中等热度', itemStyle: { color: '#e6a23c' } },
        { value: counts.HIGH, name: '高热度', itemStyle: { color: '#f56c6c' } },
        { value: counts.EXTREME, name: '极高热度', itemStyle: { color: '#c0392b' } }
      ]
    }]
  })
}

const handleResize = () => {
  predictionChart?.resize()
  heatLevelChart?.resize()
}

onMounted(async () => {
  initDate()
  await loadParkingLots()
  await nextTick()
  loadPredictionData()
  window.addEventListener('resize', handleResize)
})
</script>
