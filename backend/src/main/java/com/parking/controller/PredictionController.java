package com.parking.controller;

import com.parking.dto.Result;
import com.parking.dto.PredictionResultDTO;
import com.parking.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/predictions")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<PredictionResultDTO>> getPrediction(
            @PathVariable Long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(predictionService.getPrediction(parkingLotId, date));
    }

    @GetMapping("/parking-lot/{parkingLotId}/range")
    public Result<List<PredictionResultDTO>> getPredictionRange(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(predictionService.getPredictionRange(parkingLotId, days));
    }

    @GetMapping("/parking-lot/{parkingLotId}/summary")
    public Result<Map<String, Object>> getPredictionSummary(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(predictionService.getPredictionSummary(parkingLotId, days));
    }

    @GetMapping("/parking-lot/{parkingLotId}/heatmap")
    public Result<List<Map<String, Object>>> getHeatMapData(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(predictionService.getHeatMapData(parkingLotId, days));
    }

    @PostMapping("/parking-lot/{parkingLotId}/generate")
    public Result<List<PredictionResultDTO>> generatePrediction(
            @PathVariable Long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(predictionService.generatePrediction(parkingLotId, date));
    }
}
