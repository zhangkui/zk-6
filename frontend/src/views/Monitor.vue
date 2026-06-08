<template>
  <div class="page-container">
    <div class="page-header">
      <h2>停车场余位监测</h2>
      <p>实时监控各停车场车位使用情况，掌握城市停车资源动态</p>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #409eff;">{{ stats.totalLots || 0 }}</div>
          <div class="stat-label">停车场总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #67c23a;">{{ stats.totalSpots || 0 }}</div>
          <div class="stat-label">车位总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #e6a23c;">{{ stats.totalAvailable || 0 }}</div>
          <div class="stat-label">剩余车位</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color: #f56c6c;">{{ stats.overallOccupancy || 0 }}%</div>
          <div class="stat-label">整体占用率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="14">
        <div class="chart-card">
          <div class="chart-title">各停车场车位占用情况</div>
          <div ref="barChartRef" style="width: 100%; height: 420px;"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="chart-card">
          <div class="chart-title">区域车位分布</div>
          <div ref="pieChartRef" style="width: 100%; height: 420px;"></div>
        </div>
      </el-col>
    </el-row>

    <div class="chart-card">
      <div class="chart-title">停车场列表</div>
      <el-table :data="parkingLots" stripe border style="width: 100%">
        <el-table-column prop="name" label="停车场名称" min-width="180" />
        <el-table-column prop="district" label="所属区域" width="100" align="center" />
        <el-table-column prop="address" label="地址" min-width="220" />
        <el-table-column prop="totalSpots" label="总车位" width="90" align="center" />
        <el-table-column label="已占用" width="90" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c;">{{ row.occupiedSpots }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余车位" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600;">{{ row.availableSpots }}</span>
          </template>
        </el-table-column>
        <el-table-column label="占用率" width="160">
          <template #default="{ row }">
            <el-progress
              :percentage="row.occupancyRate"
              :color="getProgressColor(row.occupancyRate)"
              :stroke-width="14"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'OPEN'" type="success" effect="light">正常</el-tag>
            <el-tag v-else-if="row.status === 'CLOSED'" type="danger" effect="light">关闭</el-tag>
            <el-tag v-else type="warning" effect="light">维护</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getAllParkingLots, getOverallStatistics } from '../api/parkingLot'

const stats = ref({})
const parkingLots = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const getProgressColor = (percent) => {
  if (percent >= 85) return '#f56c6c'
  if (percent >= 60) return '#e6a23c'
  return '#67c23a'
}

const loadData = async () => {
  try {
    const [lotsRes, statsRes] = await Promise.all([
      getAllParkingLots(),
      getOverallStatistics()
    ])
    parkingLots.value = lotsRes.data || []
    stats.value = statsRes.data || {}
    await nextTick()
    renderCharts()
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const renderCharts = () => {
  if (barChartRef.value) {
    if (!barChart) barChart = echarts.init(barChartRef.value)
    const sorted = [...parkingLots.value].sort((a, b) => b.occupancyRate - a.occupancyRate)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['已占用', '剩余'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: sorted.map(p => p.name.length > 6 ? p.name.substring(0, 6) + '...' : p.name),
        axisLabel: { rotate: 30, interval: 0 }
      },
      yAxis: { type: 'value', name: '车位数量' },
      series: [
        {
          name: '已占用',
          type: 'bar',
          stack: 'total',
          data: sorted.map(p => p.occupiedSpots),
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '剩余',
          type: 'bar',
          stack: 'total',
          data: sorted.map(p => p.availableSpots),
          itemStyle: { color: '#67c23a' }
        }
      ]
    })
  }

  if (pieChartRef.value) {
    if (!pieChart) pieChart = echarts.init(pieChartRef.value)
    const districtData = stats.value.districtStats || []
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} 个 ({d}%)' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      series: [{
        name: '区域车位',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{d}%' },
        data: districtData.map(d => ({
          name: d.district,
          value: d.totalSpots
        }))
      }]
    })
  }
}

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})
</script>
