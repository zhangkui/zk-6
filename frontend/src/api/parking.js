import request from './request'

export const getParkingLots = () => {
  return request({
    url: '/parking-lots',
    method: 'get'
  })
}

export const getParkingLotStatus = () => {
  return request({
    url: '/parking-lots/status',
    method: 'get'
  })
}

export const getParkingLotStatusById = (id) => {
  return request({
    url: `/parking-lots/${id}/status`,
    method: 'get'
  })
}

export const getHourlyFlow = (parkingLotId, date) => {
  return request({
    url: `/traffic-flow/parking-lot/${parkingLotId}/hourly`,
    method: 'get',
    params: { date }
  })
}

export const getAvgHourlyFlow = (parkingLotId, days = 7) => {
  return request({
    url: `/traffic-flow/parking-lot/${parkingLotId}/avg-hourly`,
    method: 'get',
    params: { days }
  })
}

export const getDailyFlow = (parkingLotId, days = 7) => {
  return request({
    url: `/traffic-flow/parking-lot/${parkingLotId}/daily`,
    method: 'get',
    params: { days }
  })
}

export const getTrafficAnalysis = (parkingLotId, days = 30) => {
  return request({
    url: `/traffic-flow/parking-lot/${parkingLotId}/analysis`,
    method: 'get',
    params: { days }
  })
}

export const getPrediction = (parkingLotId, date) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}`,
    method: 'get',
    params: { date }
  })
}

export const getPredictionRange = (parkingLotId, days = 7) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}/range`,
    method: 'get',
    params: { days }
  })
}

export const getPredictionSummary = (parkingLotId, days = 7) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}/summary`,
    method: 'get',
    params: { days }
  })
}

export const getHeatMapData = (parkingLotId, days = 7) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}/heatmap`,
    method: 'get',
    params: { days }
  })
}

export const getActiveAlerts = () => {
  return request({
    url: '/alerts/active',
    method: 'get'
  })
}

export const getRecentAlerts = () => {
  return request({
    url: '/alerts/recent',
    method: 'get'
  })
}

export const getAlertCount = () => {
  return request({
    url: '/alerts/count',
    method: 'get'
  })
}

export const resolveAlert = (id) => {
  return request({
    url: `/alerts/${id}/resolve`,
    method: 'put'
  })
}

export const getSpaceSummary = (parkingLotId) => {
  return request({
    url: `/parking-spaces/parking-lot/${parkingLotId}/summary`,
    method: 'get'
  })
}

export const getSpacesByParkingLot = (parkingLotId) => {
  return request({
    url: `/parking-spaces/parking-lot/${parkingLotId}`,
    method: 'get'
  })
}

export const toggleSpaceStatus = (id) => {
  return request({
    url: `/parking-spaces/${id}/toggle`,
    method: 'put'
  })
}
