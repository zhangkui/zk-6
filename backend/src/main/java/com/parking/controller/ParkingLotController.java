package com.parking.controller;

import com.parking.dto.Result;
import com.parking.dto.ParkingLotStatusDTO;
import com.parking.entity.ParkingLot;
import com.parking.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-lots")
public class ParkingLotController {

    @Autowired
    private ParkingLotService parkingLotService;

    @GetMapping
    public Result<List<ParkingLot>> list() {
        return Result.success(parkingLotService.list());
    }

    @GetMapping("/{id}")
    public Result<ParkingLot> getById(@PathVariable Long id) {
        ParkingLot lot = parkingLotService.getById(id);
        if (lot == null) {
            return Result.error("停车场不存在");
        }
        return Result.success(lot);
    }

    @GetMapping("/status")
    public Result<List<ParkingLotStatusDTO>> getAllStatus() {
        return Result.success(parkingLotService.getAllStatus());
    }

    @GetMapping("/{id}/status")
    public Result<ParkingLotStatusDTO> getStatusById(@PathVariable Long id) {
        ParkingLotStatusDTO status = parkingLotService.getStatusById(id);
        if (status == null) {
            return Result.error("停车场不存在");
        }
        return Result.success(status);
    }

    @GetMapping("/district/{district}")
    public Result<List<ParkingLot>> getByDistrict(@PathVariable String district) {
        return Result.success(parkingLotService.getByDistrict(district));
    }

    @PutMapping("/{id}/available-spaces")
    public Result<ParkingLot> updateAvailableSpaces(@PathVariable Long id,
                                                    @RequestParam Integer availableSpaces) {
        ParkingLot updated = parkingLotService.updateAvailableSpaces(id, availableSpaces);
        if (updated == null) {
            return Result.error("停车场不存在");
        }
        return Result.success(updated);
    }
}
