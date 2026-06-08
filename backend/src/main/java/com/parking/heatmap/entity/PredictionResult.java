package com.parking.heatmap.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_result")
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_lot_id")
    private Long parkingLotId;

    @Column(name = "predict_date", nullable = false)
    private LocalDate predictDate;

    @Column(name = "predict_hour", nullable = false)
    private Integer predictHour;

    @Column(name = "predicted_occupancy", precision = 5, scale = 2)
    private BigDecimal predictedOccupancy = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "heat_level", length = 20)
    private HeatLevel heatLevel;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidence = BigDecimal.ZERO;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum HeatLevel {
        LOW, MODERATE, HIGH, EXTREME
    }

    public PredictionResult() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public LocalDate getPredictDate() { return predictDate; }
    public void setPredictDate(LocalDate predictDate) { this.predictDate = predictDate; }
    public Integer getPredictHour() { return predictHour; }
    public void setPredictHour(Integer predictHour) { this.predictHour = predictHour; }
    public BigDecimal getPredictedOccupancy() { return predictedOccupancy; }
    public void setPredictedOccupancy(BigDecimal predictedOccupancy) { this.predictedOccupancy = predictedOccupancy; }
    public HeatLevel getHeatLevel() { return heatLevel; }
    public void setHeatLevel(HeatLevel heatLevel) { this.heatLevel = heatLevel; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
