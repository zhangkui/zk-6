package com.parking.heatmap.prediction;

import com.parking.heatmap.dto.ParkingHeatmapDto;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.PredictionResult;
import com.parking.heatmap.entity.TrafficRecord;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.PredictionResultRepository;
import com.parking.heatmap.repository.TrafficRecordRepository;
import com.parking.heatmap.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    private final TrafficRecordRepository trafficRecordRepository;
    private final PredictionResultRepository predictionResultRepository;

    private static final double WEEKDAY_WEIGHT = 1.0;
    private static final double WEEKEND_WEIGHT = 1.15;
    private static final double PEAK_HOUR_BOOST = 1.25;
    private static final double RECENT_WEIGHT = 0.5;
    private static final double HISTORICAL_WEIGHT = 0.5;

    public PredictionService(TrafficRecordRepository trafficRecordRepository, PredictionResultRepository predictionResultRepository) {
        this.trafficRecordRepository = trafficRecordRepository;
        this.predictionResultRepository = predictionResultRepository;
    }

    public List<PredictionResult> predictForParkingLot(Long parkingLotId, LocalDate predictDate) {
        List<PredictionResult> results = new ArrayList<>();
        LocalDate startDate = predictDate.minusDays(14);
        LocalDate endDate = predictDate.minusDays(1);

        List<TrafficRecord> historicalRecords = trafficRecordRepository
                .findByParkingLotIdAndRecordDateBetweenOrderByRecordDateAscRecordHourAsc(
                        parkingLotId, startDate, endDate);

        Map<Integer, List<BigDecimal>> hourlyOccupancyMap = groupByHour(historicalRecords);

        boolean isWeekend = DateUtils.isWeekend(predictDate);

        for (int hour = 0; hour < 24; hour++) {
            List<BigDecimal> hourlyData = hourlyOccupancyMap.getOrDefault(hour, Collections.emptyList());
            BigDecimal predicted = calculatePrediction(hourlyData, hour, isWeekend);
            BigDecimal confidence = calculateConfidence(hourlyData.size());

            PredictionResult result = new PredictionResult();
            result.setParkingLotId(parkingLotId);
            result.setPredictDate(predictDate);
            result.setPredictHour(hour);
            result.setPredictedOccupancy(predicted);
            result.setHeatLevel(determineHeatLevel(predicted));
            result.setConfidence(confidence);
            results.add(result);
        }

        return predictionResultRepository.saveAll(results);
    }

    public void generatePredictionsForAllLots(List<Long> parkingLotIds, int daysAhead) {
        List<LocalDate> dates = DateUtils.getNextNDays(daysAhead);
        for (Long lotId : parkingLotIds) {
            for (LocalDate date : dates) {
                List<PredictionResult> existing = predictionResultRepository
                        .findByParkingLotIdAndPredictDateOrderByPredictHour(lotId, date);
                if (existing.isEmpty()) {
                    try {
                        predictForParkingLot(lotId, date);
                    } catch (Exception e) {
                        log.error("Failed to generate prediction for lot {} on {}", lotId, date, e);
                    }
                }
            }
        }
    }

    private Map<Integer, List<BigDecimal>> groupByHour(List<TrafficRecord> records) {
        Map<Integer, List<BigDecimal>> map = new HashMap<>();
        for (TrafficRecord record : records) {
            map.computeIfAbsent(record.getRecordHour(), k -> new ArrayList<>())
               .add(record.getOccupancyRate() != null ? record.getOccupancyRate() : BigDecimal.ZERO);
        }
        return map;
    }

    private BigDecimal calculatePrediction(List<BigDecimal> hourlyData, int hour, boolean isWeekend) {
        if (hourlyData.isEmpty()) {
            return estimateBasedOnHour(hour, isWeekend);
        }

        int size = hourlyData.size();
        int recentCount = Math.min(7, size);

        double recentSum = 0;
        for (int i = size - recentCount; i < size; i++) {
            recentSum += hourlyData.get(i).doubleValue();
        }
        double recentAvg = recentSum / recentCount;

        double historicalSum = 0;
        for (int i = 0; i < size - recentCount; i++) {
            historicalSum += hourlyData.get(i).doubleValue();
        }
        double historicalAvg = (size - recentCount) > 0 ? historicalSum / (size - recentCount) : recentAvg;

        double weightedAvg = recentAvg * RECENT_WEIGHT + historicalAvg * HISTORICAL_WEIGHT;

        double dayTypeFactor = isWeekend ? WEEKEND_WEIGHT : WEEKDAY_WEIGHT;
        double hourFactor = DateUtils.isPeakHour(hour) ? PEAK_HOUR_BOOST : 1.0;

        double finalValue = weightedAvg * dayTypeFactor * hourFactor;
        finalValue = Math.max(0, Math.min(100, finalValue));

        return BigDecimal.valueOf(finalValue).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal estimateBasedOnHour(int hour, boolean isWeekend) {
        double baseOccupancy;
        if (hour >= 0 && hour < 6) {
            baseOccupancy = 15.0;
        } else if (hour >= 6 && hour < 9) {
            baseOccupancy = isWeekend ? 45.0 : 70.0;
        } else if (hour >= 9 && hour < 12) {
            baseOccupancy = isWeekend ? 75.0 : 60.0;
        } else if (hour >= 12 && hour < 14) {
            baseOccupancy = isWeekend ? 85.0 : 55.0;
        } else if (hour >= 14 && hour < 17) {
            baseOccupancy = isWeekend ? 80.0 : 58.0;
        } else if (hour >= 17 && hour < 20) {
            baseOccupancy = isWeekend ? 70.0 : 75.0;
        } else {
            baseOccupancy = 35.0;
        }
        return BigDecimal.valueOf(baseOccupancy).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConfidence(int dataPoints) {
        double confidence;
        if (dataPoints >= 14) {
            confidence = 92.0;
        } else if (dataPoints >= 7) {
            confidence = 80.0;
        } else if (dataPoints >= 3) {
            confidence = 65.0;
        } else {
            confidence = 45.0;
        }
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }

    private PredictionResult.HeatLevel determineHeatLevel(BigDecimal occupancy) {
        double value = occupancy.doubleValue();
        if (value >= 85) {
            return PredictionResult.HeatLevel.EXTREME;
        } else if (value >= 65) {
            return PredictionResult.HeatLevel.HIGH;
        } else if (value >= 40) {
            return PredictionResult.HeatLevel.MODERATE;
        } else {
            return PredictionResult.HeatLevel.LOW;
        }
    }
}
