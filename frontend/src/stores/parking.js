import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getParkingLotStatus,
  getActiveAlerts,
  getAlertCount
} from '@/api/parking'

export const useParkingStore = defineStore('parking', () => {
  const parkingLots = ref([])
  const activeAlerts = ref([])
  const alertCount = ref(0)
  const selectedParkingLot = ref(null)
  const loading = ref(false)

  const totalSpaces = computed(() => {
    return parkingLots.value.reduce((sum, lot) => sum + (lot.totalSpaces || 0), 0)
  })

  const availableSpaces = computed(() => {
    return parkingLots.value.reduce((sum, lot) => sum + (lot.availableSpaces || 0), 0)
  })

  const occupiedSpaces = computed(() => {
    return totalSpaces.value - availableSpaces.value
  })

  const avgOccupancy = computed(() => {
    if (parkingLots.value.length === 0) return 0
    const sum = parkingLots.value.reduce((sum, lot) => {
      return sum + (lot.occupancyRate ? parseFloat(lot.occupancyRate) : 0)
    }, 0)
    return (sum / parkingLots.value.length).toFixed(1)
  })

  const criticalLots = computed(() => {
    return parkingLots.value.filter(lot => lot.congestionLevel === 'critical').length
  })

  const warningLots = computed(() => {
    return parkingLots.value.filter(lot => lot.congestionLevel === 'warning').length
  })

  const fetchParkingStatus = async () => {
    loading.value = true
    try {
      const data = await getParkingLotStatus()
      parkingLots.value = data || []
      if (parkingLots.value.length > 0 && !selectedParkingLot.value) {
        selectedParkingLot.value = parkingLots.value[0]
      }
    } catch (error) {
      console.error('获取停车场状态失败:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchActiveAlerts = async () => {
    try {
      const data = await getActiveAlerts()
      activeAlerts.value = data || []
    } catch (error) {
      console.error('获取活跃预警失败:', error)
    }
  }

  const fetchAlertCount = async () => {
    try {
      const data = await getAlertCount()
      alertCount.value = data?.activeCount || 0
    } catch (error) {
      console.error('获取预警数量失败:', error)
    }
  }

  const fetchAllData = async () => {
    await Promise.all([
      fetchParkingStatus(),
      fetchActiveAlerts(),
      fetchAlertCount()
    ])
  }

  const setSelectedParkingLot = (lot) => {
    selectedParkingLot.value = lot
  }

  return {
    parkingLots,
    activeAlerts,
    alertCount,
    selectedParkingLot,
    loading,
    totalSpaces,
    availableSpaces,
    occupiedSpaces,
    avgOccupancy,
    criticalLots,
    warningLots,
    fetchParkingStatus,
    fetchActiveAlerts,
    fetchAlertCount,
    fetchAllData,
    setSelectedParkingLot
  }
})
