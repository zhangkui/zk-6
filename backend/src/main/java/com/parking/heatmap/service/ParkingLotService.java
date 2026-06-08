package com.parking.heatmap.service;

import com.parking.heatmap.dto.ParkingLotDTO;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository, ParkingSpotRepository parkingSpotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSpotRepository = parkingSpotRepository;
    }

    public List<ParkingLotDTO> getAllParkingLots() {
        return parkingLotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ParkingLotDTO getParkingLotById(Long id) {
        return parkingLotRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<ParkingLotDTO> getParkingLotsByDistrict(String district) {
        return parkingLotRepository.findByDistrict(district).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getOverallStatistics() {
        List<ParkingLot> lots = parkingLotRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        int totalLots = lots.size();
        int totalSpots = lots.stream().mapToInt(ParkingLot::getTotalSpots).sum();
        int totalAvailable = lots.stream().mapToInt(ParkingLot::getAvailableSpots).sum();
        int totalOccupied = totalSpots - totalAvailable;
        BigDecimal overallOccupancy = totalSpots > 0
                ? BigDecimal.valueOf((double) totalOccupied / totalSpots * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        stats.put("totalLots", totalLots);
        stats.put("totalSpots", totalSpots);
        stats.put("totalAvailable", totalAvailable);
        stats.put("totalOccupied", totalOccupied);
        stats.put("overallOccupancy", overallOccupancy);
        stats.put("updateTime", LocalDateTime.now());

        List<Map<String, Object>> districtStats = parkingLotRepository.getDistrictStatistics().stream()
                .map(row -> {
                    Map<String, Object> ds = new HashMap<>();
                    ds.put("district", row[0]);
                    ds.put("lotCount", row[1]);
                    ds.put("totalSpots", row[2]);
                    ds.put("availableSpots", row[3]);
                    return ds;
                }).collect(Collectors.toList());
        stats.put("districtStats", districtStats);

        return stats;
    }

    @Transactional
    public ParkingLotDTO updateAvailableSpots(Long id, Integer availableSpots) {
        return parkingLotRepository.findById(id).map(lot -> {
            lot.setAvailableSpots(availableSpots);
            lot.setUpdatedAt(LocalDateTime.now());
            return convertToDTO(parkingLotRepository.save(lot));
        }).orElse(null);
    }

    @Transactional
    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        return parkingLotRepository.save(parkingLot);
    }

    private ParkingLotDTO convertToDTO(ParkingLot lot) {
        int occupied = lot.getTotalSpots() - lot.getAvailableSpots();
        BigDecimal occupancyRate = lot.getTotalSpots() > 0
                ? BigDecimal.valueOf((double) occupied / lot.getTotalSpots() * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ParkingLotDTO.builder()
                .id(lot.getId())
                .name(lot.getName())
                .address(lot.getAddress())
                .latitude(lot.getLatitude())
                .longitude(lot.getLongitude())
                .totalSpots(lot.getTotalSpots())
                .availableSpots(lot.getAvailableSpots())
                .occupiedSpots(occupied)
                .occupancyRate(occupancyRate)
                .district(lot.getDistrict())
                .status(lot.getStatus() != null ? lot.getStatus().name() : "OPEN")
                .updatedAt(lot.getUpdatedAt())
                .build();
    }
}
