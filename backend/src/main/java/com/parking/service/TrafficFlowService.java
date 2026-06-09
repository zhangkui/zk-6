package com.parking.service;

import com.parking.dto.DailyFlowDTO;
import com.parking.dto.HourlyFlowDTO;
import com.parking.entity.TrafficFlow;
import com.parking.mapper.TrafficFlowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrafficFlowService {

    @Autowired
    private TrafficFlowMapper trafficFlowMapper;

    public List<HourlyFlowDTO> getHourlyFlowByDate(Long parkingLotId, LocalDate date) {
        return trafficFlowMapper.selectHourlyFlowByDate(parkingLotId, date);
    }

    public List<HourlyFlowDTO> getAvgHourlyFlow(Long parkingLotId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return trafficFlowMapper.selectAvgHourlyFlow(parkingLotId, startDate, endDate);
    }

    public List<DailyFlowDTO> getDailyFlowRange(Long parkingLotId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return trafficFlowMapper.selectDailyFlowRange(parkingLotId, startDate, endDate);
    }

    public Map<String, Object> getTrafficAnalysis(Long parkingLotId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<DailyFlowDTO> dailyFlows = trafficFlowMapper.selectDailyFlowRange(parkingLotId, startDate, endDate);
        List<HourlyFlowDTO> avgHourly = trafficFlowMapper.selectAvgHourlyFlow(parkingLotId, startDate, endDate);

        Map<String, Object> analysis = new HashMap<>();

        int totalInflow = dailyFlows.stream().mapToInt(DailyFlowDTO::getTotalInflow).sum();
        int totalOutflow = dailyFlows.stream().mapToInt(DailyFlowDTO::getTotalOutflow).sum();

        BigDecimal avgOccupancy = dailyFlows.stream()
                .map(DailyFlowDTO::getAvgOccupancyRate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyFlows.size()), 2, RoundingMode.HALF_UP);

        HourlyFlowDTO peakHour = avgHourly.stream()
                .max(Comparator.comparing(HourlyFlowDTO::getOccupancyRate))
                .orElse(null);

        HourlyFlowDTO valleyHour = avgHourly.stream()
                .min(Comparator.comparing(HourlyFlowDTO::getOccupancyRate))
                .orElse(null);

        List<Map<String, Object>> weekdayAnalysis = getWeekdayAnalysis(parkingLotId, startDate);

        analysis.put("totalInflow", totalInflow);
        analysis.put("totalOutflow", totalOutflow);
        analysis.put("avgOccupancyRate", avgOccupancy);
        analysis.put("peakHour", peakHour);
        analysis.put("valleyHour", valleyHour);
        analysis.put("dailyFlows", dailyFlows);
        analysis.put("avgHourly", avgHourly);
        analysis.put("weekdayAnalysis", weekdayAnalysis);

        return analysis;
    }

    private List<Map<String, Object>> getWeekdayAnalysis(Long parkingLotId, LocalDate startDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (int i = 0; i < 7; i++) {
            List<HourlyFlowDTO> pattern = trafficFlowMapper.selectHistoricalPatternByDay(
                    parkingLotId, i + 1, startDate.minusDays(30));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("weekday", weekdays[i]);
            dayData.put("dayIndex", i);

            if (!pattern.isEmpty()) {
                BigDecimal avgOccupancy = pattern.stream()
                        .map(HourlyFlowDTO::getOccupancyRate)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(pattern.size()), 2, RoundingMode.HALF_UP);

                HourlyFlowDTO peak = pattern.stream()
                        .max(Comparator.comparing(HourlyFlowDTO::getOccupancyRate))
                        .orElse(null);

                dayData.put("avgOccupancy", avgOccupancy);
                dayData.put("peakHour", peak);
                dayData.put("hourlyData", pattern);
            } else {
                dayData.put("avgOccupancy", BigDecimal.ZERO);
                dayData.put("peakHour", null);
                dayData.put("hourlyData", Collections.emptyList());
            }

            result.add(dayData);
        }

        return result;
    }

    public TrafficFlow addFlowRecord(TrafficFlow flow) {
        trafficFlowMapper.insert(flow);
        return flow;
    }
}
