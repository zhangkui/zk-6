package com.parking.controller;

import com.parking.dto.Result;
import com.parking.entity.ParkingSpaceStatus;
import com.parking.service.ParkingSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking-spaces")
public class ParkingSpaceController {

    @Autowired
    private ParkingSpaceService spaceService;

    @GetMapping("/parking-lot/{parkingLotId}")
    public Result<List<ParkingSpaceStatus>> getSpacesByParkingLot(@PathVariable Long parkingLotId) {
        return Result.success(spaceService.getSpacesByParkingLot(parkingLotId));
    }

    @GetMapping("/parking-lot/{parkingLotId}/summary")
    public Result<Map<String, Object>> getSpaceSummary(@PathVariable Long parkingLotId) {
        return Result.success(spaceService.getSpaceSummary(parkingLotId));
    }

    @GetMapping("/parking-lot/{parkingLotId}/zones")
    public Result<Map<Integer, List<ParkingSpaceStatus>>> getSpacesByZone(@PathVariable Long parkingLotId) {
        return Result.success(spaceService.getSpacesByZone(parkingLotId));
    }

    @PutMapping("/{id}/status")
    public Result<ParkingSpaceStatus> updateSpaceStatus(
            @PathVariable Long id,
            @RequestParam Boolean isOccupied) {
        ParkingSpaceStatus updated = spaceService.updateSpaceStatus(id, isOccupied);
        if (updated == null) {
            return Result.error("车位不存在");
        }
        return Result.success(updated);
    }

    @PutMapping("/{id}/toggle")
    public Result<ParkingSpaceStatus> toggleSpaceStatus(@PathVariable Long id) {
        ParkingSpaceStatus updated = spaceService.toggleSpaceStatus(id);
        if (updated == null) {
            return Result.error("车位不存在");
        }
        return Result.success(updated);
    }

    @PostMapping("/parking-lot/{parkingLotId}/simulate")
    public Result<Void> simulateParkingActivity(@PathVariable Long parkingLotId) {
        spaceService.simulateParkingActivity(parkingLotId);
        return Result.success();
    }
}
