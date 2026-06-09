package com.parking.predictor;

import com.parking.dto.HourlyFlowDTO;
import com.parking.dto.PredictionResultDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ParkingHeatPredictor {

    private static final BigDecimal WEEKDAY_WEIGHT = new BigDecimal("0.6");
    private static final BigDecimal WEEKEND_WEIGHT = new BigDecimal("0.4");
    private static final BigDecimal TREND_WEIGHT = new BigDecimal("0.15");
    private static final BigDecimal RANDOM_FACTOR = new BigDecimal("0.05");

    public List<PredictionResultDTO> predict(Long parkingLotId,
                                             LocalDate predictionDate,
                                             List<HourlyFlowDTO> historicalPattern,
                                             List<HourlyFlowDTO> recentPattern,
                                             BigDecimal recentTrend) {

        List<PredictionResultDTO> results = new ArrayList<>();
        boolean isWeekend = isWeekend(predictionDate);

        Map<Integer, HourlyFlowDTO> historyMap = new HashMap<>();
        Map<Integer, HourlyFlowDTO> recentMap = new HashMap<>();

        for (HourlyFlowDTO dto : historicalPattern) {
            historyMap.put(dto.getHour(), dto);
        }
        for (HourlyFlowDTO dto : recentPattern) {
            recentMap.put(dto.getHour(), dto);
        }

        for (int hour = 0; hour < 24; hour++) {
            PredictionResultDTO result = new PredictionResultDTO();
            result.setPredictionDate(predictionDate);
            result.setHour(hour);

            HourlyFlowDTO history = historyMap.get(hour);
            HourlyFlowDTO recent = recentMap.get(hour);

            if (history == null) {
                history = createDefaultHourlyFlow(hour);
            }
            if (recent == null) {
                recent = history;
            }

            BigDecimal weight1 = isWeekend ? WEEKEND_WEIGHT : WEEKDAY_WEIGHT;
            BigDecimal weight2 = isWeekend ? WEEKEND_WEIGHT : WEEKDAY_WEIGHT;

            BigDecimal predictedOccupancy = history.getOccupancyRate()
                    .multiply(weight1)
                    .add(recent.getOccupancyRate().multiply(weight2))
                    .add(recentTrend.multiply(TREND_WEIGHT));

            double random = (Math.random() - 0.5) * RANDOM_FACTOR.doubleValue() * 100;
            predictedOccupancy = predictedOccupancy.add(BigDecimal.valueOf(random));

            predictedOccupancy = predictedOccupancy.max(BigDecimal.ZERO).min(new BigDecimal("98"));
            predictedOccupancy = predictedOccupancy.setScale(2, RoundingMode.HALF_UP);

            BigDecimal predictedInflow = BigDecimal.valueOf(history.getInflow())
                    .multiply(weight1)
                    .add(BigDecimal.valueOf(recent.getInflow()).multiply(weight2))
                    .multiply(BigDecimal.ONE.add(recentTrend.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
            predictedInflow = predictedInflow.setScale(0, RoundingMode.HALF_UP);

            BigDecimal predictedOutflow = BigDecimal.valueOf(history.getOutflow())
                    .multiply(weight1)
                    .add(BigDecimal.valueOf(recent.getOutflow()).multiply(weight2))
                    .multiply(BigDecimal.ONE.add(recentTrend.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
            predictedOutflow = predictedOutflow.setScale(0, RoundingMode.HALF_UP);

            BigDecimal confidence = calculateConfidence(hour, isWeekend, recentTrend);

            result.setPredictedOccupancyRate(predictedOccupancy);
            result.setPredictedInflow(predictedInflow.intValue());
            result.setPredictedOutflow(predictedOutflow.intValue());
            result.setConfidence(confidence);
            result.setHeatLevel(getHeatLevel(predictedOccupancy));

            results.add(result);
        }

        return results;
    }

    private HourlyFlowDTO createDefaultHourlyFlow(int hour) {
        HourlyFlowDTO dto = new HourlyFlowDTO();
        dto.setHour(hour);

        int baseOccupancy = switch (hour) {
            case 0, 1, 2, 3, 4, 5 -> 15;
            case 6 -> 25;
            case 7, 8 -> 65;
            case 9, 10 -> 55;
            case 11, 12, 13 -> 75;
            case 14, 15, 16 -> 50;
            case 17, 18, 19 -> 80;
            case 20, 21 -> 60;
            case 22, 23 -> 30;
            default -> 40;
        };

        dto.setInflow(baseOccupancy);
        dto.setOutflow(baseOccupancy - 5);
        dto.setOccupancyRate(BigDecimal.valueOf(baseOccupancy));
        return dto;
    }

    private BigDecimal calculateConfidence(int hour, boolean isWeekend, BigDecimal trend) {
        BigDecimal baseConfidence = new BigDecimal("85");

        if (hour >= 7 && hour <= 9 || hour >= 17 && hour <= 19) {
            baseConfidence = baseConfidence.subtract(new BigDecimal("5"));
        }

        if (isWeekend) {
            baseConfidence = baseConfidence.subtract(new BigDecimal("3"));
        }

        BigDecimal trendAbs = trend.abs();
        if (trendAbs.compareTo(new BigDecimal("10")) > 0) {
            baseConfidence = baseConfidence.subtract(new BigDecimal("5"));
        }

        return baseConfidence.max(new BigDecimal("70")).setScale(2, RoundingMode.HALF_UP);
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

    private boolean isWeekend(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    public BigDecimal calculateRecentTrend(List<HourlyFlowDTO> recentDays, int days) {
        if (recentDays == null || recentDays.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        for (HourlyFlowDTO dto : recentDays) {
            if (dto.getOccupancyRate() != null) {
                sum = sum.add(dto.getOccupancyRate());
                count++;
            }
        }

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
}
