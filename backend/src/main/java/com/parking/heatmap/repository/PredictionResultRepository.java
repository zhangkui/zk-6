package com.parking.heatmap.repository;

import com.parking.heatmap.entity.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

    List<PredictionResult> findByParkingLotIdAndPredictDateOrderByPredictHour(Long parkingLotId, LocalDate predictDate);

    List<PredictionResult> findByParkingLotIdAndPredictDateBetweenOrderByPredictDateAscPredictHourAsc(
            Long parkingLotId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM PredictionResult p WHERE p.predictDate = :date ORDER BY p.predictHour")
    List<PredictionResult> findAllByPredictDate(@Param("date") LocalDate date);

    @Query("SELECT p.parkingLotId, p.predictHour, AVG(p.predictedOccupancy), MAX(p.heatLevel) " +
           "FROM PredictionResult p WHERE p.predictDate BETWEEN :startDate AND :endDate " +
           "GROUP BY p.parkingLotId, p.predictHour")
    List<Object[]> getHeatmapData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
