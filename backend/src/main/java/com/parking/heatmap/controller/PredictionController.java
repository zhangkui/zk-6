package com.parking.heatmap.controller;

import com.parking.heatmap.dto.PredictionDTO;
import com.parking.heatmap.dto.Result;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.PredictionResult;
import com.parking.heatmap.prediction.PredictionService;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.PredictionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final PredictionResultRepository predictionResultRepository;
    private final ParkingLotRepository parkingLotRepository;

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<PredictionDTO>> getPredictionsByLotAndDate(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        String lotName = lot != null ? lot.getName() : "";

        List<PredictionResult> results = predictionResultRepository
                .findByParkingLotIdAndPredictDateOrderByPredictHour(parkingLotId, date);

        if (results.isEmpty()) {
            results = predictionService.predictForParkingLot(parkingLotId, date);
        }

        List<PredictionDTO> dtos = results.stream()
                .map(r -> convertToDTO(r, lotName))
                .collect(Collectors.toList());

        return Result.success(dtos);
    }

    @GetMapping("/parking-lot/{parkingLotId}/range")
    public Result<List<PredictionDTO>> getPredictionsByLotAndDateRange(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        String lotName = lot != null ? lot.getName() : "";

        List<PredictionResult> results = predictionResultRepository
                .findByParkingLotIdAndPredictDateBetweenOrderByPredictDateAscPredictHourAsc(
                        parkingLotId, startDate, endDate);

        List<PredictionDTO> dtos = results.stream()
                .map(r -> convertToDTO(r, lotName))
                .collect(Collectors.toList());

        return Result.success(dtos);
    }

    @PostMapping("/generate")
    public Result<String> generatePredictions(
            @RequestParam(defaultValue = "7") int daysAhead) {
        List<Long> lotIds = parkingLotRepository.findAll().stream()
                .map(ParkingLot::getId)
                .collect(Collectors.toList());
        predictionService.generatePredictionsForAllLots(lotIds, daysAhead);
        return Result.success("预测生成完成，共处理 " + lotIds.size() + " 个停车场，预测 " + daysAhead + " 天数据");
    }

    private PredictionDTO convertToDTO(PredictionResult result, String lotName) {
        return PredictionDTO.builder()
                .parkingLotId(result.getParkingLotId())
                .parkingLotName(lotName)
                .predictDate(result.getPredictDate())
                .predictHour(result.getPredictHour())
                .predictedOccupancy(result.getPredictedOccupancy())
                .heatLevel(result.getHeatLevel() != null ? result.getHeatLevel().name() : "LOW")
                .confidence(result.getConfidence())
                .build();
    }
}
