import * as echarts from 'echarts'

export const initChart = (el, option) => {
  const chart = echarts.init(el)
  chart.setOption(option)

  const resizeObserver = new ResizeObserver(() => {
    chart.resize()
  })
  resizeObserver.observe(el)

  return {
    chart,
    resizeObserver,
    setOption: (newOption) => {
      chart.setOption(newOption, true)
    },
    destroy: () => {
      resizeObserver.disconnect()
      chart.dispose()
    }
  }
}

export const getGaugeOption = (value, title, color = '#1890ff') => {
  return {
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 5,
      itemStyle: {
        color: color
      },
      progress: {
        show: true,
        width: 18
      },
      pointer: {
        show: false
      },
      axisLine: {
        lineStyle: {
          width: 18,
          color: [[1, '#e6e6e6']]
        }
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: false
      },
      axisLabel: {
        show: false
      },
      anchor: {
        show: false
      },
      title: {
        show: true,
        offsetCenter: [0, '70%'],
        fontSize: 14,
        color: '#606266'
      },
      detail: {
        valueAnimation: true,
        fontSize: 32,
        fontWeight: 'bold',
        offsetCenter: [0, '30%'],
        formatter: '{value}%',
        color: color
      },
      data: [{
        value: value,
        name: title
      }]
    }]
  }
}

export const getLineOption = (xData, seriesData, title = '') => {
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
  return {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 500
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: seriesData.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#ebeef5'
        }
      }
    },
    series: seriesData.map((s, index) => ({
      name: s.name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: s.data,
      itemStyle: {
        color: colors[index % colors.length]
      },
      lineStyle: {
        width: 3,
        color: colors[index % colors.length]
      },
      areaStyle: s.area ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors[index % colors.length] + '40' },
          { offset: 1, color: colors[index % colors.length] + '05' }
        ])
      } : undefined
    }))
  }
}

export const getBarOption = (xData, seriesData, title = '') => {
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
  return {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 500
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: seriesData.map(s => s.name),
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        color: '#606266'
      },
      splitLine: {
        lineStyle: {
          color: '#ebeef5'
        }
      }
    },
    series: seriesData.map((s, index) => ({
      name: s.name,
      type: 'bar',
      barWidth: '40%',
      data: s.data,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors[index % colors.length] },
          { offset: 1, color: colors[index % colors.length] + '80' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }))
  }
}

export const getPieOption = (data, title = '') => {
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4']
  return {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 500
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '55%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 18,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: data.map((item, index) => ({
        value: item.value,
        name: item.name,
        itemStyle: {
          color: colors[index % colors.length]
        }
      }))
    }]
  }
}

export const getHeatmapOption = (xData, yData, data, title = '') => {
  return {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 500
      }
    },
    tooltip: {
      position: 'top',
      formatter: (params) => {
        return `${yData[params.value[1]]} ${xData[params.value[0]]}<br/>热度: ${params.value[2]}%`
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '15%',
      top: '15%'
    },
    xAxis: {
      type: 'category',
      data: xData,
      splitArea: {
        show: true
      },
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: yData,
      splitArea: {
        show: true
      },
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '5%',
      inRange: {
        color: ['#52c41a', '#faad14', '#f5222d']
      },
      formatter: (value) => `${value}%`
    },
    series: [{
      name: '热度',
      type: 'heatmap',
      data: data,
      label: {
        show: true,
        formatter: (params) => `${params.value[2]}%`,
        fontSize: 10
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      }
    }]
  }
}

export const getCongestionColor = (level) => {
  const colors = {
    critical: '#f56c6c',
    warning: '#e6a23c',
    moderate: '#409eff',
    normal: '#67c23a'
  }
  return colors[level] || '#909399'
}

export const getHeatLevelColor = (level) => {
  const colors = {
    extreme: '#f5222d',
    high: '#fa8c16',
    medium: '#faad14',
    low: '#52c41a',
    idle: '#8c8c8c'
  }
  return colors[level] || '#909399'
}

export const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export const getHours = () => {
  return Array.from({ length: 24 }, (_, i) => `${String(i).padStart(2, '0')}:00`)
}
