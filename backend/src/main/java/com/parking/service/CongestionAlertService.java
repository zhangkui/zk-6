package com.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.dto.CongestionAlertDTO;
import com.parking.dto.PredictionResultDTO;
import com.parking.entity.CongestionAlert;
import com.parking.entity.ParkingLot;
import com.parking.mapper.CongestionAlertMapper;
import com.parking.mapper.ParkingLotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CongestionAlertService {

    @Autowired
    private CongestionAlertMapper alertMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Value("${parking.congestion.warning-threshold:80.0}")
    private BigDecimal warningThreshold;

    @Value("${parking.congestion.danger-threshold:90.0}")
    private BigDecimal dangerThreshold;

    public List<CongestionAlertDTO> getActiveAlerts() {
        return alertMapper.selectActiveAlerts();
    }

    public List<CongestionAlertDTO> getAlertsByParkingLot(Long parkingLotId) {
        return alertMapper.selectAlertsByParkingLot(parkingLotId);
    }

    public List<CongestionAlertDTO> getRecentAlerts() {
        return alertMapper.selectRecentAlerts();
    }

    @Transactional
    public CongestionAlert createAlert(CongestionAlert alert) {
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsResolved(false);
        alertMapper.insert(alert);
        return alert;
    }

    @Transactional
    public CongestionAlert resolveAlert(Long id) {
        CongestionAlert alert = alertMapper.selectById(id);
        if (alert == null) {
            return null;
        }
        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alertMapper.updateById(alert);
        return alert;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkRealTimeCongestion() {
        List<ParkingLot> parkingLots = parkingLotMapper.selectList(null);

        for (ParkingLot lot : parkingLots) {
            if (lot.getTotalSpaces() == null || lot.getTotalSpaces() == 0) continue;

            BigDecimal occupancyRate = BigDecimal.valueOf(lot.getTotalSpaces() - lot.getAvailableSpaces())
                    .divide(BigDecimal.valueOf(lot.getTotalSpaces()), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));

            String alertLevel = null;
            String message = null;

            if (occupancyRate.compareTo(dangerThreshold) >= 0) {
                alertLevel = "high";
                message = String.format("停车场[%s]当前使用率已达%.1f%%，即将满位！", lot.getName(), occupancyRate);
            } else if (occupancyRate.compareTo(warningThreshold) >= 0) {
                alertLevel = "medium";
                message = String.format("停车场[%s]当前使用率已达%.1f%%，请注意。", lot.getName(), occupancyRate);
            }

            if (alertLevel != null) {
                LambdaQueryWrapper<CongestionAlert> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(CongestionAlert::getParkingLotId, lot.getId())
                        .eq(CongestionAlert::getAlertType, "real_time")
                        .eq(CongestionAlert::getIsResolved, false)
                        .ge(CongestionAlert::getCreatedAt, LocalDateTime.now().minusHours(1));

                Long existing = alertMapper.selectCount(wrapper);

                if (existing == null || existing == 0) {
                    CongestionAlert alert = new CongestionAlert();
                    alert.setParkingLotId(lot.getId());
                    alert.setAlertType("real_time");
                    alert.setAlertLevel(alertLevel);
                    alert.setMessage(message);
                    alert.setOccupancyRate(occupancyRate.setScale(2, BigDecimal.ROUND_HALF_UP));
                    alert.setPredictedTime(LocalDateTime.now());
                    createAlert(alert);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void generatePredictionAlerts() {
        List<ParkingLot> parkingLots = parkingLotMapper.selectList(null);
        LocalDate predictionDate = LocalDate.now();

        for (ParkingLot lot : parkingLots) {
            List<PredictionResultDTO> predictions = predictionService.getPrediction(lot.getId(), predictionDate);

            List<PredictionResultDTO> highRiskPeriods = new ArrayList<>();
            for (PredictionResultDTO p : predictions) {
                if (p.getPredictedOccupancyRate().compareTo(warningThreshold) >= 0) {
                    highRiskPeriods.add(p);
                }
            }

            if (!highRiskPeriods.isEmpty()) {
                int startHour = highRiskPeriods.get(0).getHour();
                int endHour = highRiskPeriods.get(highRiskPeriods.size() - 1).getHour();

                BigDecimal maxOccupancy = highRiskPeriods.stream()
                        .map(PredictionResultDTO::getPredictedOccupancyRate)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                String alertLevel = maxOccupancy.compareTo(dangerThreshold) >= 0 ? "high" : "medium";
                String message = String.format("预计今日%02d:00-%02d:00停车场[%s]将出现拥堵，预计最高使用率%.1f%%",
                        startHour, endHour + 1, lot.getName(), maxOccupancy);

                CongestionAlert alert = new CongestionAlert();
                alert.setParkingLotId(lot.getId());
                alert.setAlertType("prediction");
                alert.setAlertLevel(alertLevel);
                alert.setMessage(message);
                alert.setOccupancyRate(maxOccupancy.setScale(2, BigDecimal.ROUND_HALF_UP));
                alert.setPredictedTime(LocalDateTime.of(predictionDate, LocalTime.of(startHour, 0)));
                createAlert(alert);
            }
        }
    }

    public List<CongestionAlert> generatePredictionAlertsForLot(Long parkingLotId, LocalDate date) {
        List<PredictionResultDTO> predictions = predictionService.getPrediction(parkingLotId, date);
        List<CongestionAlert> alerts = new ArrayList<>();

        List<PredictionResultDTO> highRiskPeriods = new ArrayList<>();
        for (PredictionResultDTO p : predictions) {
            if (p.getPredictedOccupancyRate().compareTo(warningThreshold) >= 0) {
                highRiskPeriods.add(p);
            }
        }

        if (!highRiskPeriods.isEmpty()) {
            ParkingLot lot = parkingLotService.getById(parkingLotId);
            int startHour = highRiskPeriods.get(0).getHour();
            int endHour = highRiskPeriods.get(highRiskPeriods.size() - 1).getHour();

            BigDecimal maxOccupancy = highRiskPeriods.stream()
                    .map(PredictionResultDTO::getPredictedOccupancyRate)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            String alertLevel = maxOccupancy.compareTo(dangerThreshold) >= 0 ? "high" : "medium";
            String message = String.format("预计%s %02d:00-%02d:00停车场[%s]将出现拥堵，预计最高使用率%.1f%%",
                    date, startHour, endHour + 1, lot.getName(), maxOccupancy);

            CongestionAlert alert = new CongestionAlert();
            alert.setParkingLotId(parkingLotId);
            alert.setAlertType("prediction");
            alert.setAlertLevel(alertLevel);
            alert.setMessage(message);
            alert.setOccupancyRate(maxOccupancy.setScale(2, BigDecimal.ROUND_HALF_UP));
            alert.setPredictedTime(LocalDateTime.of(date, LocalTime.of(startHour, 0)));
            alerts.add(createAlert(alert));
        }

        return alerts;
    }

    public long getActiveAlertCount() {
        LambdaQueryWrapper<CongestionAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CongestionAlert::getIsResolved, false);
        Long count = alertMapper.selectCount(wrapper);
        return count != null ? count : 0;
    }
}
