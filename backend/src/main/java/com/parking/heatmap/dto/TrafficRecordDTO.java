package com.parking.heatmap.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrafficRecordDTO {

    private Long parkingLotId;
    private String parkingLotName;
    private LocalDate recordDate;
    private Integer recordHour;
    private Integer entryCount;
    private Integer exitCount;
    private BigDecimal occupancyRate;

    public TrafficRecordDTO() {}

    private TrafficRecordDTO(Builder b) {
        this.parkingLotId = b.parkingLotId; this.parkingLotName = b.parkingLotName;
        this.recordDate = b.recordDate; this.recordHour = b.recordHour;
        this.entryCount = b.entryCount; this.exitCount = b.exitCount;
        this.occupancyRate = b.occupancyRate;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long parkingLotId; private String parkingLotName;
        private LocalDate recordDate; private Integer recordHour;
        private Integer entryCount; private Integer exitCount;
        private BigDecimal occupancyRate;
        public Builder parkingLotId(Long v) { parkingLotId = v; return this; }
        public Builder parkingLotName(String v) { parkingLotName = v; return this; }
        public Builder recordDate(LocalDate v) { recordDate = v; return this; }
        public Builder recordHour(Integer v) { recordHour = v; return this; }
        public Builder entryCount(Integer v) { entryCount = v; return this; }
        public Builder exitCount(Integer v) { exitCount = v; return this; }
        public Builder occupancyRate(BigDecimal v) { occupancyRate = v; return this; }
        public TrafficRecordDTO build() { return new TrafficRecordDTO(this); }
    }

    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
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
}
