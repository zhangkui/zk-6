package com.parking.heatmap.repository;

import com.parking.heatmap.entity.WarningAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WarningAlertRepository extends JpaRepository<WarningAlert, Long> {

    List<WarningAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<WarningAlert> findByParkingLotIdOrderByCreatedAtDesc(Long parkingLotId);

    List<WarningAlert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);

    List<WarningAlert> findByAlertLevelAndIsResolvedFalseOrderByCreatedAtDesc(WarningAlert.AlertLevel alertLevel);
}
