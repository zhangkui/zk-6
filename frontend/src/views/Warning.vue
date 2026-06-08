<template>
  <div class="page-container">
    <div class="page-header">
      <h2>拥堵预警展示</h2>
      <p>实时监控停车场拥堵状况，及时处理各类预警信息</p>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #c0392b;">
          <div class="stat-value" style="color: #c0392b;">{{ alertCounts.DANGER || 0 }}</div>
          <div class="stat-label">严重预警</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #f56c6c;">
          <div class="stat-value" style="color: #f56c6c;">{{ alertCounts.WARNING || 0 }}</div>
          <div class="stat-label">警告预警</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #409eff;">
          <div class="stat-value" style="color: #409eff;">{{ alertCounts.INFO || 0 }}</div>
          <div class="stat-label">提示预警</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #67c23a;">
          <div class="stat-value" style="color: #67c23a;">{{ totalActive }}</div>
          <div class="stat-label">未处理预警总数</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="10">
        <div class="chart-card">
          <div class="chart-title">预警类型分布</div>
          <div ref="typeChartRef" style="width: 100%; height: 320px;"></div>
        </div>
      </el-col>
      <el-col :span="14">
        <div class="chart-card">
          <div class="chart-title">预警等级趋势（最近24小时）</div>
          <div ref="trendChartRef" style="width: 100%; height: 320px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
        <div class="chart-title" style="margin-bottom: 0; border-bottom: none; padding-bottom: 0;">未处理预警列表</div>
        <div>
          <el-radio-group v-model="filterLevel" size="default" @change="filterWarnings">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="DANGER">严重</el-radio-button>
            <el-radio-button value="WARNING">警告</el-radio-button>
            <el-radio-button value="INFO">提示</el-radio-button>
          </el-radio-group>
          <el-button style="margin-left: 12px;" type="primary" @click="refreshWarnings">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>

      <el-table :data="filteredWarnings" stripe border style="width: 100%" max-height="480">
        <el-table-column label="预警等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.alertLevel === 'DANGER'" type="danger" effect="dark">严重</el-tag>
            <el-tag v-else-if="row.alertLevel === 'WARNING'" type="warning" effect="dark">警告</el-tag>
            <el-tag v-else type="info" effect="dark">提示</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getAlertTypeTag(row.alertType)" effect="light">
              {{ getAlertTypeLabel(row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parkingLotName" label="停车场" min-width="160" />
        <el-table-column prop="message" label="预警信息" min-width="300" />
        <el-table-column label="预警时间" width="180" align="center">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isResolved" type="success" effect="light">已处理</el-tag>
            <el-tag v-else type="danger" effect="light">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isResolved"
              type="primary"
              size="small"
              link
              @click="handleResolve(row)">
              标记已处理
            </el-button>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllActiveWarnings, resolveWarning } from '../api/warning'

const warnings = ref([])
const filteredWarnings = ref([])
const filterLevel = ref('')
const typeChartRef = ref(null)
const trendChartRef = ref(null)
let typeChart = null
let trendChart = null

const alertCounts = computed(() => {
  const counts = { DANGER: 0, WARNING: 0, INFO: 0 }
  warnings.value.forEach(w => {
    if (!w.isResolved && counts[w.alertLevel] !== undefined) {
      counts[w.alertLevel]++
    }
  })
  return counts
})

const totalActive = computed(() => {
  return warnings.value.filter(w => !w.isResolved).length
})

const getAlertTypeLabel = (type) => {
  const map = {
    OVERCROWDING: '过度拥挤',
    LOW_AVAILABILITY: '车位不足',
    ABNORMAL_TRAFFIC: '异常车流',
    MAINTENANCE: '维护中'
  }
  return map[type] || type
}

const getAlertTypeTag = (type) => {
  const map = {
    OVERCROWDING: 'danger',
    LOW_AVAILABILITY: 'warning',
    ABNORMAL_TRAFFIC: 'warning',
    MAINTENANCE: 'info'
  }
  return map[type] || 'info'
}

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const filterWarnings = () => {
  if (!filterLevel.value) {
    filteredWarnings.value = warnings.value.filter(w => !w.isResolved)
  } else {
    filteredWarnings.value = warnings.value.filter(w => !w.isResolved && w.alertLevel === filterLevel.value)
  }
}

const refreshWarnings = async () => {
  try {
    const res = await getAllActiveWarnings()
    warnings.value = res.data || []
    filterWarnings()
    await nextTick()
    renderTypeChart()
    renderTrendChart()
  } catch (e) {
    console.error(e)
  }
}

const handleResolve = async (row) => {
  try {
    await ElMessageBox.confirm('确定将此预警标记为已处理吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await resolveWarning(row.id)
    if (res.code === 200) {
      ElMessage.success('已标记为处理')
      refreshWarnings()
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const renderTypeChart = () => {
  if (!typeChartRef.value) return
  if (!typeChart) typeChart = echarts.init(typeChartRef.value)

  const counts = { OVERCROWDING: 0, LOW_AVAILABILITY: 0, ABNORMAL_TRAFFIC: 0, MAINTENANCE: 0 }
  warnings.value.filter(w => !w.isResolved).forEach(w => {
    if (counts[w.alertType] !== undefined) counts[w.alertType]++
  })

  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}条 ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: [
        { value: counts.OVERCROWDING, name: '过度拥挤', itemStyle: { color: '#c0392b' } },
        { value: counts.LOW_AVAILABILITY, name: '车位不足', itemStyle: { color: '#f56c6c' } },
        { value: counts.ABNORMAL_TRAFFIC, name: '异常车流', itemStyle: { color: '#e6a23c' } },
        { value: counts.MAINTENANCE, name: '维护中', itemStyle: { color: '#409eff' } }
      ]
    }]
  })
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)

  const now = new Date()
  const hours = []
  const dangerData = []
  const warningData = []
  const infoData = []

  for (let i = 23; i >= 0; i--) {
    const d = new Date(now.getTime() - i * 60 * 60 * 1000)
    const hourKey = `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()} ${d.getHours()}`
    hours.push(`${d.getHours()}:00`)

    const hourAlerts = warnings.value.filter(w => {
      const wd = new Date(w.createdAt)
      const wk = `${wd.getFullYear()}-${wd.getMonth() + 1}-${wd.getDate()} ${wd.getHours()}`
      return wk === hourKey
    })

    dangerData.push(hourAlerts.filter(w => w.alertLevel === 'DANGER').length)
    warningData.push(hourAlerts.filter(w => w.alertLevel === 'WARNING').length)
    infoData.push(hourAlerts.filter(w => w.alertLevel === 'INFO').length)
  }

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['严重', '警告', '提示'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: hours, boundaryGap: false },
    yAxis: { type: 'value', name: '预警数量' },
    series: [
      { name: '严重', type: 'line', smooth: true, data: dangerData, itemStyle: { color: '#c0392b' }, areaStyle: { color: 'rgba(192,57,43,0.2)' } },
      { name: '警告', type: 'line', smooth: true, data: warningData, itemStyle: { color: '#f56c6c' }, areaStyle: { color: 'rgba(245,108,108,0.2)' } },
      { name: '提示', type: 'line', smooth: true, data: infoData, itemStyle: { color: '#409eff' }, areaStyle: { color: 'rgba(64,158,255,0.2)' } }
    ]
  })
}

const handleResize = () => {
  typeChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  await refreshWarnings()
  window.addEventListener('resize', handleResize)
})
</script>
