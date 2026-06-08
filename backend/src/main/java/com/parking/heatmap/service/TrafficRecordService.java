package com.parking.heatmap.service;

import com.parking.heatmap.dto.TrafficRecordDTO;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.TrafficRecord;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.TrafficRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrafficRecordService {

    private final TrafficRecordRepository trafficRecordRepository;
    private final ParkingLotRepository parkingLotRepository;

    public TrafficRecordService(TrafficRecordRepository trafficRecordRepository, ParkingLotRepository parkingLotRepository) {
        this.trafficRecordRepository = trafficRecordRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public List<TrafficRecordDTO> getTrafficByLotAndDate(Long parkingLotId, LocalDate date) {
        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        String lotName = lot != null ? lot.getName() : "";

        return trafficRecordRepository.findByParkingLotIdAndRecordDateOrderByRecordHour(parkingLotId, date)
                .stream()
                .map(r -> convertToDTO(r, lotName))
                .collect(Collectors.toList());
    }

    public List<TrafficRecordDTO> getTrafficByLotAndDateRange(Long parkingLotId, LocalDate startDate, LocalDate endDate) {
        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        String lotName = lot != null ? lot.getName() : "";

        return trafficRecordRepository.findByParkingLotIdAndRecordDateBetweenOrderByRecordDateAscRecordHourAsc(
                        parkingLotId, startDate, endDate)
                .stream()
                .map(r -> convertToDTO(r, lotName))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getHourlyAverageStats(Long parkingLotId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = trafficRecordRepository.getHourlyAverageStats(parkingLotId, startDate, endDate);

        List<Integer> hours = new ArrayList<>();
        List<BigDecimal> avgOccupancy = new ArrayList<>();
        List<Integer> totalEntries = new ArrayList<>();
        List<Integer> totalExits = new ArrayList<>();

        for (Object[] row : rawData) {
            hours.add((Integer) row[0]);
            avgOccupancy.add((BigDecimal) row[1]);
            Long entries = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            Long exits = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            totalEntries.add(entries.intValue());
            totalExits.add(exits.intValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hours", hours);
        result.put("avgOccupancy", avgOccupancy);
        result.put("totalEntries", totalEntries);
        result.put("totalExits", totalExits);
        return result;
    }

    public Map<String, Object> getDailyStats(Long parkingLotId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rawData = trafficRecordRepository.getDailyStats(parkingLotId, startDate, endDate);

        List<String> dates = new ArrayList<>();
        List<Integer> totalEntries = new ArrayList<>();
        List<Integer> totalExits = new ArrayList<>();
        List<BigDecimal> avgOccupancy = new ArrayList<>();

        for (Object[] row : rawData) {
            dates.add(row[0].toString());
            Long entries = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            Long exits = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            totalEntries.add(entries.intValue());
            totalExits.add(exits.intValue());
            avgOccupancy.add((BigDecimal) row[3]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("totalEntries", totalEntries);
        result.put("totalExits", totalExits);
        result.put("avgOccupancy", avgOccupancy);
        return result;
    }

    public TrafficRecord createTrafficRecord(TrafficRecord record) {
        return trafficRecordRepository.save(record);
    }

    public List<TrafficRecord> createTrafficRecords(List<TrafficRecord> records) {
        return trafficRecordRepository.saveAll(records);
    }

    private TrafficRecordDTO convertToDTO(TrafficRecord record, String lotName) {
        return TrafficRecordDTO.builder()
                .parkingLotId(record.getParkingLotId())
                .parkingLotName(lotName)
                .recordDate(record.getRecordDate())
                .recordHour(record.getRecordHour())
                .entryCount(record.getEntryCount())
                .exitCount(record.getExitCount())
                .occupancyRate(record.getOccupancyRate())
                .build();
    }
}
