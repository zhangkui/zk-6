package com.parking.heatmap.service;

import com.parking.heatmap.dto.ParkingHeatmapDto;
import com.parking.heatmap.prediction.HeatmapPredictionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ParkingHeatmapService {

    private final HeatmapPredictionService heatmapPredictionService;

    public ParkingHeatmapService(HeatmapPredictionService heatmapPredictionService) {
        this.heatmapPredictionService = heatmapPredictionService;
    }

    public List<ParkingHeatmapDto> getHeatmapForDate(LocalDate date) {
        return heatmapPredictionService.getHeatmapData(date);
    }

    public List<ParkingHeatmapDto> getHeatmapForDateAndDistrict(LocalDate date, String district) {
        return heatmapPredictionService.getHeatmapDataByDistrict(date, district);
    }

    public Map<String, BigDecimal> getDistrictAverageHeat(LocalDate date) {
        return heatmapPredictionService.getDistrictAverageHeat(date);
    }
}
