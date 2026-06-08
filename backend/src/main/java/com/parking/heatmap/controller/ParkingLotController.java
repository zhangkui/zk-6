package com.parking.heatmap.controller;

import com.parking.heatmap.dto.ParkingLotDTO;
import com.parking.heatmap.dto.Result;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @GetMapping
    public Result<List<ParkingLotDTO>> getAllParkingLots() {
        return Result.success(parkingLotService.getAllParkingLots());
    }

    @GetMapping("/{id}")
    public Result<ParkingLotDTO> getParkingLotById(@PathVariable Long id) {
        ParkingLotDTO dto = parkingLotService.getParkingLotById(id);
        return dto != null ? Result.success(dto) : Result.error("停车场不存在");
    }

    @GetMapping("/district/{district}")
    public Result<List<ParkingLotDTO>> getParkingLotsByDistrict(@PathVariable String district) {
        return Result.success(parkingLotService.getParkingLotsByDistrict(district));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOverallStatistics() {
        return Result.success(parkingLotService.getOverallStatistics());
    }

    @PutMapping("/{id}/available-spots")
    public Result<ParkingLotDTO> updateAvailableSpots(
            @PathVariable Long id,
            @RequestParam Integer availableSpots) {
        ParkingLotDTO dto = parkingLotService.updateAvailableSpots(id, availableSpots);
        return dto != null ? Result.success(dto) : Result.error("停车场不存在");
    }

    @PostMapping
    public Result<ParkingLot> createParkingLot(@RequestBody ParkingLot parkingLot) {
        return Result.success(parkingLotService.createParkingLot(parkingLot));
    }
}
