package com.parking.controller;

import com.parking.dto.Result;
import com.parking.dto.DailyFlowDTO;
import com.parking.dto.HourlyFlowDTO;
import com.parking.entity.TrafficFlow;
import com.parking.service.TrafficFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/traffic-flow")
public class TrafficFlowController {

    @Autowired
    private TrafficFlowService trafficFlowService;

    @GetMapping("/parking-lot/{parkingLotId}/hourly")
    public Result<List<HourlyFlowDTO>> getHourlyFlow(
            @PathVariable Long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(trafficFlowService.getHourlyFlowByDate(parkingLotId, date));
    }

    @GetMapping("/parking-lot/{parkingLotId}/avg-hourly")
    public Result<List<HourlyFlowDTO>> getAvgHourlyFlow(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(trafficFlowService.getAvgHourlyFlow(parkingLotId, days));
    }

    @GetMapping("/parking-lot/{parkingLotId}/daily")
    public Result<List<DailyFlowDTO>> getDailyFlow(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(trafficFlowService.getDailyFlowRange(parkingLotId, days));
    }

    @GetMapping("/parking-lot/{parkingLotId}/analysis")
    public Result<Map<String, Object>> getTrafficAnalysis(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(trafficFlowService.getTrafficAnalysis(parkingLotId, days));
    }

    @PostMapping
    public Result<TrafficFlow> addFlowRecord(@RequestBody TrafficFlow flow) {
        return Result.success(trafficFlowService.addFlowRecord(flow));
    }
}
