package com.parking.heatmap.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warning_alert")
public class WarningAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_lot_id")
    private Long parkingLotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 50)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", length = 20)
    private AlertLevel alertLevel;

    @Column(length = 500)
    private String message;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum AlertType {
        OVERCROWDING, LOW_AVAILABILITY, ABNORMAL_TRAFFIC, MAINTENANCE
    }

    public enum AlertLevel {
        INFO, WARNING, DANGER
    }

    public WarningAlert() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public AlertLevel getAlertLevel() { return alertLevel; }
    public void setAlertLevel(AlertLevel alertLevel) { this.alertLevel = alertLevel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
