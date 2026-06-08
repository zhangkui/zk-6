package com.parking.heatmap.controller;

import com.parking.heatmap.dto.Result;
import com.parking.heatmap.dto.TrafficRecordDTO;
import com.parking.heatmap.entity.TrafficRecord;
import com.parking.heatmap.service.TrafficRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficRecordService trafficRecordService;

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<TrafficRecordDTO>> getTrafficByLotAndDate(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(trafficRecordService.getTrafficByLotAndDate(parkingLotId, date));
    }

    @GetMapping("/parking-lot/{parkingLotId}/range")
    public Result<List<TrafficRecordDTO>> getTrafficByLotAndDateRange(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(trafficRecordService.getTrafficByLotAndDateRange(parkingLotId, startDate, endDate));
    }

    @GetMapping("/parking-lot/{parkingLotId}/hourly-stats")
    public Result<Map<String, Object>> getHourlyAverageStats(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(trafficRecordService.getHourlyAverageStats(parkingLotId, startDate, endDate));
    }

    @GetMapping("/parking-lot/{parkingLotId}/daily-stats")
    public Result<Map<String, Object>> getDailyStats(
            @PathVariable Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(trafficRecordService.getDailyStats(parkingLotId, startDate, endDate));
    }

    @PostMapping
    public Result<TrafficRecord> createTrafficRecord(@RequestBody TrafficRecord record) {
        return Result.success(trafficRecordService.createTrafficRecord(record));
    }

    @PostMapping("/batch")
    public Result<List<TrafficRecord>> createTrafficRecords(@RequestBody List<TrafficRecord> records) {
        return Result.success(trafficRecordService.createTrafficRecords(records));
    }
}
