package com.parking.service;

import com.parking.dto.HourlyFlowDTO;
import com.parking.dto.PredictionResultDTO;
import com.parking.entity.PredictionRecord;
import com.parking.mapper.PredictionRecordMapper;
import com.parking.mapper.TrafficFlowMapper;
import com.parking.predictor.ParkingHeatPredictor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictionService {

    @Autowired
    private ParkingHeatPredictor predictor;

    @Autowired
    private TrafficFlowMapper trafficFlowMapper;

    @Autowired
    private PredictionRecordMapper predictionRecordMapper;

    @Value("${parking.prediction.model-version:v1.0}")
    private String modelVersion;

    @Value("${parking.prediction.default-confidence:85.0}")
    private BigDecimal defaultConfidence;

    @Transactional
    public List<PredictionResultDTO> generatePrediction(Long parkingLotId, LocalDate predictionDate) {
        LocalDate historyStart = predictionDate.minusDays(30);
        int dayOfWeek = predictionDate.getDayOfWeek().getValue();

        List<HourlyFlowDTO> historicalPattern = trafficFlowMapper.selectHistoricalPatternByDay(
                parkingLotId, dayOfWeek, historyStart);

        LocalDate recentStart = predictionDate.minusDays(7);
        LocalDate recentEnd = predictionDate.minusDays(1);
        List<HourlyFlowDTO> recentPattern = trafficFlowMapper.selectAvgHourlyFlow(
                parkingLotId, recentStart, recentEnd);

        List<HourlyFlowDTO> recentDaysFlow = trafficFlowMapper.selectAvgHourlyFlow(
                parkingLotId, predictionDate.minusDays(3), predictionDate.minusDays(1));
        BigDecimal recentTrend = predictor.calculateRecentTrend(recentDaysFlow, 3);

        List<PredictionResultDTO> predictions = predictor.predict(
                parkingLotId, predictionDate, historicalPattern, recentPattern, recentTrend);

        savePredictionRecords(parkingLotId, predictionDate, predictions);

        return predictions;
    }

    @Transactional
    public void savePredictionRecords(Long parkingLotId, LocalDate predictionDate,
                                      List<PredictionResultDTO> predictions) {
        predictionRecordMapper.deleteByParkingLotAndDate(parkingLotId, predictionDate);

        for (PredictionResultDTO dto : predictions) {
            PredictionRecord record = new PredictionRecord();
            record.setParkingLotId(parkingLotId);
            record.setPredictionDate(predictionDate);
            record.setHour(dto.getHour());
            record.setPredictedOccupancyRate(dto.getPredictedOccupancyRate());
            record.setPredictedInflow(dto.getPredictedInflow());
            record.setPredictedOutflow(dto.getPredictedOutflow());
            record.setConfidence(dto.getConfidence());
            record.setModelVersion(modelVersion);
            predictionRecordMapper.insert(record);
        }
    }

    public List<PredictionResultDTO> getPrediction(Long parkingLotId, LocalDate date) {
        List<PredictionRecord> records = predictionRecordMapper.selectByParkingLotAndDate(parkingLotId, date);

        if (records.isEmpty()) {
            return generatePrediction(parkingLotId, date);
        }

        return convertToDTO(records);
    }

    public List<PredictionResultDTO> getPredictionRange(Long parkingLotId, int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days - 1);

        List<PredictionResultDTO> allResults = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<PredictionResultDTO> daily = getPrediction(parkingLotId, date);
            allResults.addAll(daily);
        }

        return allResults;
    }

    public Map<String, Object> getPredictionSummary(Long parkingLotId, int days) {
        List<PredictionResultDTO> predictions = getPredictionRange(parkingLotId, days);

        Map<String, Object> summary = new HashMap<>();

        Map<LocalDate, List<PredictionResultDTO>> groupedByDate = new HashMap<>();
        for (PredictionResultDTO p : predictions) {
            groupedByDate.computeIfAbsent(p.getPredictionDate(), k -> new ArrayList<>()).add(p);
        }

        List<Map<String, Object>> dailySummaries = new ArrayList<>();
        for (Map.Entry<LocalDate, List<PredictionResultDTO>> entry : groupedByDate.entrySet()) {
            Map<String, Object> daySummary = new HashMap<>();
            List<PredictionResultDTO> dayPredictions = entry.getValue();

            PredictionResultDTO peak = dayPredictions.stream()
                    .max((a, b) -> a.getPredictedOccupancyRate().compareTo(b.getPredictedOccupancyRate()))
                    .orElse(null);

            double avgOccupancy = dayPredictions.stream()
                    .mapToDouble(p -> p.getPredictedOccupancyRate().doubleValue())
                    .average()
                    .orElse(0);

            long highHeatHours = dayPredictions.stream()
                    .filter(p -> p.getHeatLevel().equals("high") || p.getHeatLevel().equals("extreme"))
                    .count();

            daySummary.put("date", entry.getKey());
            daySummary.put("avgOccupancy", BigDecimal.valueOf(avgOccupancy).setScale(2, BigDecimal.ROUND_HALF_UP));
            daySummary.put("peakPrediction", peak);
            daySummary.put("highHeatHours", highHeatHours);
            daySummary.put("hourlyPredictions", dayPredictions);

            dailySummaries.add(daySummary);
        }

        summary.put("dailySummaries", dailySummaries);
        summary.put("totalDays", days);
        summary.put("parkingLotId", parkingLotId);

        return summary;
    }

    public List<Map<String, Object>> getHeatMapData(Long parkingLotId, int days) {
        List<PredictionResultDTO> predictions = getPredictionRange(parkingLotId, days);
        List<Map<String, Object>> heatMapData = new ArrayList<>();

        for (PredictionResultDTO p : predictions) {
            Map<String, Object> data = new HashMap<>();
            data.put("date", p.getPredictionDate().toString());
            data.put("hour", p.getHour());
            data.put("occupancy", p.getPredictedOccupancyRate());
            data.put("heatLevel", p.getHeatLevel());
            data.put("confidence", p.getConfidence());
            heatMapData.add(data);
        }

        return heatMapData;
    }

    private List<PredictionResultDTO> convertToDTO(List<PredictionRecord> records) {
        List<PredictionResultDTO> dtos = new ArrayList<>();
        for (PredictionRecord record : records) {
            PredictionResultDTO dto = new PredictionResultDTO();
            dto.setPredictionDate(record.getPredictionDate());
            dto.setHour(record.getHour());
            dto.setPredictedOccupancyRate(record.getPredictedOccupancyRate());
            dto.setPredictedInflow(record.getPredictedInflow());
            dto.setPredictedOutflow(record.getPredictedOutflow());
            dto.setConfidence(record.getConfidence());
            dto.setHeatLevel(getHeatLevel(record.getPredictedOccupancyRate()));
            dtos.add(dto);
        }
        return dtos;
    }

    private String getHeatLevel(BigDecimal occupancy) {
        if (occupancy.compareTo(new BigDecimal("90")) >= 0) {
            return "extreme";
        } else if (occupancy.compareTo(new BigDecimal("75")) >= 0) {
            return "high";
        } else if (occupancy.compareTo(new BigDecimal("50")) >= 0) {
            return "medium";
        } else if (occupancy.compareTo(new BigDecimal("25")) >= 0) {
            return "low";
        } else {
            return "idle";
        }
    }
}
