import request from './request'

export const getHeatmap = (date) => {
  return request({
    url: '/heatmap',
    method: 'get',
    params: date ? { date } : {}
  })
}

export const getHeatmapByDistrict = (district, date) => {
  return request({
    url: `/heatmap/district/${district}`,
    method: 'get',
    params: date ? { date } : {}
  })
}

export const getDistrictAverageHeat = (date) => {
  return request({
    url: '/heatmap/district-average',
    method: 'get',
    params: date ? { date } : {}
  })
}
