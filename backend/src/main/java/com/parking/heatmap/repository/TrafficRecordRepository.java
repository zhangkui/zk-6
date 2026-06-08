package com.parking.heatmap.repository;

import com.parking.heatmap.entity.TrafficRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrafficRecordRepository extends JpaRepository<TrafficRecord, Long> {

    List<TrafficRecord> findByParkingLotIdAndRecordDateOrderByRecordHour(Long parkingLotId, LocalDate recordDate);

    List<TrafficRecord> findByParkingLotIdAndRecordDateBetweenOrderByRecordDateAscRecordHourAsc(
            Long parkingLotId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t.recordHour, AVG(t.occupancyRate), SUM(t.entryCount), SUM(t.exitCount) " +
           "FROM TrafficRecord t WHERE t.parkingLotId = :parkingLotId " +
           "AND t.recordDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.recordHour ORDER BY t.recordHour")
    List<Object[]> getHourlyAverageStats(
            @Param("parkingLotId") Long parkingLotId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.recordDate, SUM(t.entryCount), SUM(t.exitCount), AVG(t.occupancyRate) " +
           "FROM TrafficRecord t WHERE t.parkingLotId = :parkingLotId " +
           "AND t.recordDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.recordDate ORDER BY t.recordDate")
    List<Object[]> getDailyStats(
            @Param("parkingLotId") Long parkingLotId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM TrafficRecord t WHERE t.recordDate = :date ORDER BY t.recordHour")
    List<TrafficRecord> findAllByRecordDate(@Param("date") LocalDate date);
}
