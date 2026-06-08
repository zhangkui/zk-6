import request from './request'

export const getTrafficByLotAndDate = (parkingLotId, date) => {
  return request({
    url: `/traffic/parking-lot/${parkingLotId}`,
    method: 'get',
    params: { date }
  })
}

export const getTrafficByLotAndDateRange = (parkingLotId, startDate, endDate) => {
  return request({
    url: `/traffic/parking-lot/${parkingLotId}/range`,
    method: 'get',
    params: { startDate, endDate }
  })
}

export const getHourlyAverageStats = (parkingLotId, startDate, endDate) => {
  return request({
    url: `/traffic/parking-lot/${parkingLotId}/hourly-stats`,
    method: 'get',
    params: { startDate, endDate }
  })
}

export const getDailyStats = (parkingLotId, startDate, endDate) => {
  return request({
    url: `/traffic/parking-lot/${parkingLotId}/daily-stats`,
    method: 'get',
    params: { startDate, endDate }
  })
}
