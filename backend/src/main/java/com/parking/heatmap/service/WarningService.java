package com.parking.heatmap.service;

import com.parking.heatmap.dto.WarningDTO;
import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.WarningAlert;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.WarningAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WarningService {

    private static final Logger log = LoggerFactory.getLogger(WarningService.class);

    private final WarningAlertRepository warningAlertRepository;
    private final ParkingLotRepository parkingLotRepository;

    public WarningService(WarningAlertRepository warningAlertRepository, ParkingLotRepository parkingLotRepository) {
        this.warningAlertRepository = warningAlertRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public List<WarningDTO> getAllActiveWarnings() {
        Map<Long, String> lotNames = parkingLotRepository.findAll().stream()
                .collect(Collectors.toMap(ParkingLot::getId, ParkingLot::getName));

        return warningAlertRepository.findByIsResolvedFalseOrderByCreatedAtDesc()
                .stream()
                .map(w -> convertToDTO(w, lotNames.get(w.getParkingLotId())))
                .collect(Collectors.toList());
    }

    public List<WarningDTO> getWarningsByParkingLot(Long parkingLotId) {
        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        String lotName = lot != null ? lot.getName() : "";

        return warningAlertRepository.findByParkingLotIdOrderByCreatedAtDesc(parkingLotId)
                .stream()
                .map(w -> convertToDTO(w, lotName))
                .collect(Collectors.toList());
    }

    public List<WarningDTO> getWarningsByLevel(WarningAlert.AlertLevel level) {
        Map<Long, String> lotNames = parkingLotRepository.findAll().stream()
                .collect(Collectors.toMap(ParkingLot::getId, ParkingLot::getName));

        return warningAlertRepository.findByAlertLevelAndIsResolvedFalseOrderByCreatedAtDesc(level)
                .stream()
                .map(w -> convertToDTO(w, lotNames.get(w.getParkingLotId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public WarningDTO resolveWarning(Long id) {
        return warningAlertRepository.findById(id).map(warning -> {
            warning.setIsResolved(true);
            WarningAlert saved = warningAlertRepository.save(warning);
            ParkingLot lot = parkingLotRepository.findById(saved.getParkingLotId()).orElse(null);
            String lotName = lot != null ? lot.getName() : "";
            return convertToDTO(saved, lotName);
        }).orElse(null);
    }

    public WarningAlert createWarning(WarningAlert alert) {
        return warningAlertRepository.save(alert);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndGenerateWarnings() {
        log.info("Starting warning check at {}", LocalDateTime.now());
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();

        for (ParkingLot lot : parkingLots) {
            if (lot.getTotalSpots() == null || lot.getTotalSpots() == 0) continue;

            BigDecimal occupancyRate = BigDecimal.valueOf(
                    (double) (lot.getTotalSpots() - lot.getAvailableSpots()) / lot.getTotalSpots() * 100
            );

            if (occupancyRate.doubleValue() >= 90) {
                createWarningIfNotExists(lot, WarningAlert.AlertType.OVERCROWDING,
                        WarningAlert.AlertLevel.DANGER,
                        String.format("停车场[%s]车位极度紧张，剩余车位仅%d个",
                                lot.getName(), lot.getAvailableSpots()));
            } else if (occupancyRate.doubleValue() >= 75) {
                createWarningIfNotExists(lot, WarningAlert.AlertType.LOW_AVAILABILITY,
                        WarningAlert.AlertLevel.WARNING,
                        String.format("停车场[%s]车位紧张，剩余车位%d个",
                                lot.getName(), lot.getAvailableSpots()));
            }

            if (lot.getStatus() == ParkingLot.Status.MAINTENANCE) {
                createWarningIfNotExists(lot, WarningAlert.AlertType.MAINTENANCE,
                        WarningAlert.AlertLevel.INFO,
                        String.format("停车场[%s]正在维护中", lot.getName()));
            }
        }
        log.info("Warning check completed");
    }

    private void createWarningIfNotExists(ParkingLot lot, WarningAlert.AlertType type,
                                          WarningAlert.AlertLevel level, String message) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<WarningAlert> recent = warningAlertRepository.findByParkingLotIdOrderByCreatedAtDesc(lot.getId());

        boolean exists = recent.stream()
                .anyMatch(w -> !w.getIsResolved()
                        && w.getAlertType() == type
                        && w.getAlertLevel() == level
                        && w.getCreatedAt().isAfter(threshold));

        if (!exists) {
            WarningAlert alert = new WarningAlert();
            alert.setParkingLotId(lot.getId());
            alert.setAlertType(type);
            alert.setAlertLevel(level);
            alert.setMessage(message);
            alert.setIsResolved(false);
            warningAlertRepository.save(alert);
            log.info("Created warning: {}", message);
        }
    }

    private WarningDTO convertToDTO(WarningAlert alert, String lotName) {
        return WarningDTO.builder()
                .id(alert.getId())
                .parkingLotId(alert.getParkingLotId())
                .parkingLotName(lotName)
                .alertType(alert.getAlertType() != null ? alert.getAlertType().name() : "")
                .alertLevel(alert.getAlertLevel() != null ? alert.getAlertLevel().name() : "")
                .message(alert.getMessage())
                .isResolved(alert.getIsResolved())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
