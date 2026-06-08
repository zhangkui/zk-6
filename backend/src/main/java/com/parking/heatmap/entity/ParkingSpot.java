package com.parking.heatmap.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_spot")
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_lot_id")
    private Long parkingLotId;

    @Column(name = "spot_code", nullable = false, length = 50)
    private String spotCode;

    @Column(name = "is_occupied")
    private Boolean isOccupied = false;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public ParkingSpot() {}

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getSpotCode() { return spotCode; }
    public void setSpotCode(String spotCode) { this.spotCode = spotCode; }
    public Boolean getIsOccupied() { return isOccupied; }
    public void setIsOccupied(Boolean isOccupied) { this.isOccupied = isOccupied; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
