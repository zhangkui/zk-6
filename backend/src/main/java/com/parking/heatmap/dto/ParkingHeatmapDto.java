package com.parking.heatmap.dto;

import java.math.BigDecimal;

public class ParkingHeatmapDto {

    private Long parkingLotId;
    private String parkingLotName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer hour;
    private BigDecimal heatValue;
    private String heatLevel;
    private String district;

    public ParkingHeatmapDto() {}

    private ParkingHeatmapDto(Builder b) {
        this.parkingLotId = b.parkingLotId; this.parkingLotName = b.parkingLotName;
        this.latitude = b.latitude; this.longitude = b.longitude;
        this.hour = b.hour; this.heatValue = b.heatValue;
        this.heatLevel = b.heatLevel; this.district = b.district;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long parkingLotId; private String parkingLotName;
        private BigDecimal latitude; private BigDecimal longitude;
        private Integer hour; private BigDecimal heatValue;
        private String heatLevel; private String district;
        public Builder parkingLotId(Long v) { parkingLotId = v; return this; }
        public Builder parkingLotName(String v) { parkingLotName = v; return this; }
        public Builder latitude(BigDecimal v) { latitude = v; return this; }
        public Builder longitude(BigDecimal v) { longitude = v; return this; }
        public Builder hour(Integer v) { hour = v; return this; }
        public Builder heatValue(BigDecimal v) { heatValue = v; return this; }
        public Builder heatLevel(String v) { heatLevel = v; return this; }
        public Builder district(String v) { district = v; return this; }
        public ParkingHeatmapDto build() { return new ParkingHeatmapDto(this); }
    }

    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }
    public BigDecimal getHeatValue() { return heatValue; }
    public void setHeatValue(BigDecimal heatValue) { this.heatValue = heatValue; }
    public String getHeatLevel() { return heatLevel; }
    public void setHeatLevel(String heatLevel) { this.heatLevel = heatLevel; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
}
