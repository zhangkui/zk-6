import request from './request'

export const getAllParkingLots = () => {
  return request({ url: '/parking-lots', method: 'get' })
}

export const getParkingLotById = (id) => {
  return request({ url: `/parking-lots/${id}`, method: 'get' })
}

export const getParkingLotsByDistrict = (district) => {
  return request({ url: `/parking-lots/district/${district}`, method: 'get' })
}

export const getOverallStatistics = () => {
  return request({ url: '/parking-lots/statistics', method: 'get' })
}

export const updateAvailableSpots = (id, availableSpots) => {
  return request({
    url: `/parking-lots/${id}/available-spots`,
    method: 'put',
    params: { availableSpots }
  })
}

export const createParkingLot = (data) => {
  return request({ url: '/parking-lots', method: 'post', data })
}
