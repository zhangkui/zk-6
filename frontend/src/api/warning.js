import request from './request'

export const getAllActiveWarnings = () => {
  return request({ url: '/warnings/active', method: 'get' })
}

export const getWarningsByParkingLot = (parkingLotId) => {
  return request({ url: `/warnings/parking-lot/${parkingLotId}`, method: 'get' })
}

export const getWarningsByLevel = (level) => {
  return request({ url: `/warnings/level/${level}`, method: 'get' })
}

export const resolveWarning = (id) => {
  return request({ url: `/warnings/${id}/resolve`, method: 'put' })
}
