package com.parking.controller;

import com.parking.dto.Result;
import com.parking.dto.CongestionAlertDTO;
import com.parking.entity.CongestionAlert;
import com.parking.service.CongestionAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class CongestionAlertController {

    @Autowired
    private CongestionAlertService alertService;

    @GetMapping("/active")
    public Result<List<CongestionAlertDTO>> getActiveAlerts() {
        return Result.success(alertService.getActiveAlerts());
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<CongestionAlertDTO>> getAlertsByParkingLot(@PathVariable Long parkingLotId) {
        return Result.success(alertService.getAlertsByParkingLot(parkingLotId));
    }

    @GetMapping("/recent")
    public Result<List<CongestionAlertDTO>> getRecentAlerts() {
        return Result.success(alertService.getRecentAlerts());
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> getAlertCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeCount", alertService.getActiveAlertCount());
        return Result.success(result);
    }

    @PostMapping
    public Result<CongestionAlert> createAlert(@RequestBody CongestionAlert alert) {
        return Result.success(alertService.createAlert(alert));
    }

    @PutMapping("/{id}/resolve")
    public Result<CongestionAlert> resolveAlert(@PathVariable Long id) {
        CongestionAlert resolved = alertService.resolveAlert(id);
        if (resolved == null) {
            return Result.error("预警不存在");
        }
        return Result.success(resolved);
    }

    @PostMapping("/parking-lot/{parkingLotId}/generate")
    public Result<List<CongestionAlert>> generateAlerts(
            @PathVariable Long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(alertService.generatePredictionAlertsForLot(parkingLotId, date));
    }
}
