package com.parking.heatmap.prediction;

import com.parking.heatmap.dto.ParkingHeatmapDto;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.PredictionResult;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.PredictionResultRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HeatmapPredictionService {

    private final PredictionResultRepository predictionResultRepository;
    private final ParkingLotRepository parkingLotRepository;

    public HeatmapPredictionService(PredictionResultRepository predictionResultRepository, ParkingLotRepository parkingLotRepository) {
        this.predictionResultRepository = predictionResultRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public List<ParkingHeatmapDto> getHeatmapData(LocalDate date) {
        List<PredictionResult> predictions = predictionResultRepository.findAllByPredictDate(date);
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();

        Map<Long, ParkingLot> lotMap = parkingLots.stream()
                .collect(Collectors.toMap(ParkingLot::getId, lot -> lot));

        List<ParkingHeatmapDto> heatmapData = new ArrayList<>();

        for (PredictionResult prediction : predictions) {
            ParkingLot lot = lotMap.get(prediction.getParkingLotId());
            if (lot == null) continue;

            ParkingHeatmapDto dto = ParkingHeatmapDto.builder()
                    .parkingLotId(lot.getId())
                    .parkingLotName(lot.getName())
                    .latitude(lot.getLatitude())
                    .longitude(lot.getLongitude())
                    .district(lot.getDistrict())
                    .hour(prediction.getPredictHour())
                    .heatValue(prediction.getPredictedOccupancy())
                    .heatLevel(prediction.getHeatLevel() != null ? prediction.getHeatLevel().name() : "LOW")
                    .build();
            heatmapData.add(dto);
        }

        return heatmapData;
    }

    public List<ParkingHeatmapDto> getHeatmapDataByDistrict(LocalDate date, String district) {
        return getHeatmapData(date).stream()
                .filter(dto -> district.equalsIgnoreCase(dto.getDistrict()))
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getDistrictAverageHeat(LocalDate date) {
        List<ParkingHeatmapDto> allData = getHeatmapData(date);
        return allData.stream()
                .collect(Collectors.groupingBy(
                        ParkingHeatmapDto::getDistrict,
                        Collectors.collectingAndThen(
                                Collectors.averagingDouble(d -> d.getHeatValue().doubleValue()),
                                avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                        )
                ));
    }
}
