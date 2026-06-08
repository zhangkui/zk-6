package com.parking.heatmap.controller;

import com.parking.heatmap.dto.Result;
import com.parking.heatmap.dto.WarningDTO;
import com.parking.heatmap.entity.WarningAlert;
import com.parking.heatmap.service.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warnings")
@RequiredArgsConstructor
public class WarningController {

    private final WarningService warningService;

    @GetMapping("/active")
    public Result<List<WarningDTO>> getAllActiveWarnings() {
        return Result.success(warningService.getAllActiveWarnings());
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<WarningDTO>> getWarningsByParkingLot(@PathVariable Long parkingLotId) {
        return Result.success(warningService.getWarningsByParkingLot(parkingLotId));
    }

    @GetMapping("/level/{level}")
    public Result<List<WarningDTO>> getWarningsByLevel(@PathVariable WarningAlert.AlertLevel level) {
        return Result.success(warningService.getWarningsByLevel(level));
    }

    @PutMapping("/{id}/resolve")
    public Result<WarningDTO> resolveWarning(@PathVariable Long id) {
        WarningDTO dto = warningService.resolveWarning(id);
        return dto != null ? Result.success(dto) : Result.error("预警不存在");
    }

    @PostMapping
    public Result<WarningAlert> createWarning(@RequestBody WarningAlert alert) {
        return Result.success(warningService.createWarning(alert));
    }
}
