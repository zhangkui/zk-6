package com.parking.heatmap.repository;

import com.parking.heatmap.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    List<ParkingLot> findByDistrict(String district);

    List<ParkingLot> findByStatus(ParkingLot.Status status);

    @Query("SELECT p.district, COUNT(p), SUM(p.totalSpots), SUM(p.availableSpots) FROM ParkingLot p GROUP BY p.district")
    List<Object[]> getDistrictStatistics();
}
