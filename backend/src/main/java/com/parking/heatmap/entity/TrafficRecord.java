package com.parking.heatmap.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_record")
public class TrafficRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_lot_id")
    private Long parkingLotId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "record_hour", nullable = false)
    private Integer recordHour;

    @Column(name = "entry_count")
    private Integer entryCount = 0;

    @Column(name = "exit_count")
    private Integer exitCount = 0;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    private BigDecimal occupancyRate = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TrafficRecord() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public Integer getRecordHour() { return recordHour; }
    public void setRecordHour(Integer recordHour) { this.recordHour = recordHour; }
    public Integer getEntryCount() { return entryCount; }
    public void setEntryCount(Integer entryCount) { this.entryCount = entryCount; }
    public Integer getExitCount() { return exitCount; }
    public void setExitCount(Integer exitCount) { this.exitCount = exitCount; }
    public BigDecimal getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(BigDecimal occupancyRate) { this.occupancyRate = occupancyRate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
