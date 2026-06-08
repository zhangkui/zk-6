package com.parking.heatmap.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingLotDTO {

    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer totalSpots;
    private Integer availableSpots;
    private Integer occupiedSpots;
    private BigDecimal occupancyRate;
    private String district;
    private String status;
    private LocalDateTime updatedAt;

    public ParkingLotDTO() {}

    private ParkingLotDTO(Builder b) {
        this.id = b.id; this.name = b.name; this.address = b.address;
        this.latitude = b.latitude; this.longitude = b.longitude;
        this.totalSpots = b.totalSpots; this.availableSpots = b.availableSpots;
        this.occupiedSpots = b.occupiedSpots; this.occupancyRate = b.occupancyRate;
        this.district = b.district; this.status = b.status; this.updatedAt = b.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String address;
        private BigDecimal latitude; private BigDecimal longitude;
        private Integer totalSpots; private Integer availableSpots;
        private Integer occupiedSpots; private BigDecimal occupancyRate;
        private String district; private String status; private LocalDateTime updatedAt;
        public Builder id(Long v) { id = v; return this; }
        public Builder name(String v) { name = v; return this; }
        public Builder address(String v) { address = v; return this; }
        public Builder latitude(BigDecimal v) { latitude = v; return this; }
        public Builder longitude(BigDecimal v) { longitude = v; return this; }
        public Builder totalSpots(Integer v) { totalSpots = v; return this; }
        public Builder availableSpots(Integer v) { availableSpots = v; return this; }
        public Builder occupiedSpots(Integer v) { occupiedSpots = v; return this; }
        public Builder occupancyRate(BigDecimal v) { occupancyRate = v; return this; }
        public Builder district(String v) { district = v; return this; }
        public Builder status(String v) { status = v; return this; }
        public Builder updatedAt(LocalDateTime v) { updatedAt = v; return this; }
        public ParkingLotDTO build() { return new ParkingLotDTO(this); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getTotalSpots() { return totalSpots; }
    public void setTotalSpots(Integer totalSpots) { this.totalSpots = totalSpots; }
    public Integer getAvailableSpots() { return availableSpots; }
    public void setAvailableSpots(Integer availableSpots) { this.availableSpots = availableSpots; }
    public Integer getOccupiedSpots() { return occupiedSpots; }
    public void setOccupiedSpots(Integer occupiedSpots) { this.occupiedSpots = occupiedSpots; }
    public BigDecimal getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(BigDecimal occupancyRate) { this.occupancyRate = occupancyRate; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
