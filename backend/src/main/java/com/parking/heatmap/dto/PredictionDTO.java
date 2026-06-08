package com.parking.heatmap.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PredictionDTO {

    private Long parkingLotId;
    private String parkingLotName;
    private LocalDate predictDate;
    private Integer predictHour;
    private BigDecimal predictedOccupancy;
    private String heatLevel;
    private BigDecimal confidence;

    public PredictionDTO() {}

    private PredictionDTO(Builder b) {
        this.parkingLotId = b.parkingLotId; this.parkingLotName = b.parkingLotName;
        this.predictDate = b.predictDate; this.predictHour = b.predictHour;
        this.predictedOccupancy = b.predictedOccupancy; this.heatLevel = b.heatLevel;
        this.confidence = b.confidence;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long parkingLotId; private String parkingLotName;
        private LocalDate predictDate; private Integer predictHour;
        private BigDecimal predictedOccupancy; private String heatLevel;
        private BigDecimal confidence;
        public Builder parkingLotId(Long v) { parkingLotId = v; return this; }
        public Builder parkingLotName(String v) { parkingLotName = v; return this; }
        public Builder predictDate(LocalDate v) { predictDate = v; return this; }
        public Builder predictHour(Integer v) { predictHour = v; return this; }
        public Builder predictedOccupancy(BigDecimal v) { predictedOccupancy = v; return this; }
        public Builder heatLevel(String v) { heatLevel = v; return this; }
        public Builder confidence(BigDecimal v) { confidence = v; return this; }
        public PredictionDTO build() { return new PredictionDTO(this); }
    }

    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
    public LocalDate getPredictDate() { return predictDate; }
    public void setPredictDate(LocalDate predictDate) { this.predictDate = predictDate; }
    public Integer getPredictHour() { return predictHour; }
    public void setPredictHour(Integer predictHour) { this.predictHour = predictHour; }
    public BigDecimal getPredictedOccupancy() { return predictedOccupancy; }
    public void setPredictedOccupancy(BigDecimal predictedOccupancy) { this.predictedOccupancy = predictedOccupancy; }
    public String getHeatLevel() { return heatLevel; }
    public void setHeatLevel(String heatLevel) { this.heatLevel = heatLevel; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
}
