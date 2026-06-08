package com.parking.heatmap.controller;

import com.parking.heatmap.dto.ParkingHeatmapDto;
import com.parking.heatmap.dto.Result;
import com.parking.heatmap.service.ParkingHeatmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/heatmap")
@RequiredArgsConstructor
public class ParkingHeatmapController {

    private final ParkingHeatmapService parkingHeatmapService;

    @GetMapping
    public Result<List<ParkingHeatmapDto>> getHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(parkingHeatmapService.getHeatmapForDate(date));
    }

    @GetMapping("/district/{district}")
    public Result<List<ParkingHeatmapDto>> getHeatmapByDistrict(
            @PathVariable String district,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(parkingHeatmapService.getHeatmapForDateAndDistrict(date, district));
    }

    @GetMapping("/district-average")
    public Result<Map<String, BigDecimal>> getDistrictAverageHeat(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(parkingHeatmapService.getDistrictAverageHeat(date));
    }
}
