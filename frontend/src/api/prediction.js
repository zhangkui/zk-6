import request from './request'

export const getPredictionsByLotAndDate = (parkingLotId, date) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}`,
    method: 'get',
    params: { date }
  })
}

export const getPredictionsByLotAndDateRange = (parkingLotId, startDate, endDate) => {
  return request({
    url: `/predictions/parking-lot/${parkingLotId}/range`,
    method: 'get',
    params: { startDate, endDate }
  })
}

export const generatePredictions = (daysAhead = 7) => {
  return request({
    url: '/predictions/generate',
    method: 'post',
    params: { daysAhead }
  })
}
