package com.parking.service;

import com.parking.entity.ParkingSpaceStatus;
import com.parking.mapper.ParkingSpaceStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParkingSpaceService {

    @Autowired
    private ParkingSpaceStatusMapper spaceStatusMapper;

    @Autowired
    private ParkingLotService parkingLotService;

    public List<ParkingSpaceStatus> getSpacesByParkingLot(Long parkingLotId) {
        return spaceStatusMapper.selectByParkingLotId(parkingLotId);
    }

    public Map<String, Object> getSpaceSummary(Long parkingLotId) {
        Map<String, Object> summary = new HashMap<>();

        Long occupied = spaceStatusMapper.countByOccupied(parkingLotId, true);
        Long available = spaceStatusMapper.countByOccupied(parkingLotId, false);
        long total = occupied + available;

        double occupancyRate = total > 0 ? (double) occupied / total * 100 : 0;

        summary.put("totalSpaces", total);
        summary.put("occupiedSpaces", occupied);
        summary.put("availableSpaces", available);
        summary.put("occupancyRate", Math.round(occupancyRate * 100) / 100.0);

        return summary;
    }

    @Transactional
    public ParkingSpaceStatus updateSpaceStatus(Long id, boolean isOccupied) {
        ParkingSpaceStatus status = spaceStatusMapper.selectById(id);
        if (status == null) {
            return null;
        }

        int updated = spaceStatusMapper.updateStatus(id, isOccupied);
        if (updated > 0) {
            status.setIsOccupied(isOccupied);
            status.setLastUpdated(LocalDateTime.now());

            Long parkingLotId = status.getParkingLotId();
            Long available = spaceStatusMapper.countByOccupied(parkingLotId, false);
            parkingLotService.updateAvailableSpaces(parkingLotId, available.intValue());
        }

        return status;
    }

    @Transactional
    public ParkingSpaceStatus toggleSpaceStatus(Long id) {
        ParkingSpaceStatus status = spaceStatusMapper.selectById(id);
        if (status == null) {
            return null;
        }
        return updateSpaceStatus(id, !status.getIsOccupied());
    }

    @Transactional
    public void simulateParkingActivity(Long parkingLotId) {
        List<ParkingSpaceStatus> spaces = spaceStatusMapper.selectByParkingLotId(parkingLotId);
        if (spaces.isEmpty()) return;

        int changes = (int) (Math.random() * 5) + 1;

        for (int i = 0; i < changes; i++) {
            int randomIndex = (int) (Math.random() * spaces.size());
            ParkingSpaceStatus space = spaces.get(randomIndex);

            boolean newStatus = Math.random() > 0.5;
            if (!newStatus == space.getIsOccupied()) {
                updateSpaceStatus(space.getId(), newStatus);
            }
        }
    }

    public Map<Integer, List<ParkingSpaceStatus>> getSpacesByZone(Long parkingLotId) {
        List<ParkingSpaceStatus> spaces = getSpacesByParkingLot(parkingLotId);
        Map<Integer, List<ParkingSpaceStatus>> zones = new HashMap<>();

        for (ParkingSpaceStatus space : spaces) {
            int zone = getZoneFromSpaceNumber(space.getSpaceNumber());
            zones.computeIfAbsent(zone, k -> new java.util.ArrayList<>()).add(space);
        }

        return zones;
    }

    private int getZoneFromSpaceNumber(String spaceNumber) {
        try {
            if (spaceNumber.startsWith("A")) {
                return Integer.parseInt(spaceNumber.substring(1, 2));
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
}
