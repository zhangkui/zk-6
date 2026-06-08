package com.parking.heatmap.dto;

import java.time.LocalDateTime;

public class WarningDTO {

    private Long id;
    private Long parkingLotId;
    private String parkingLotName;
    private String alertType;
    private String alertLevel;
    private String message;
    private Boolean isResolved;
    private LocalDateTime createdAt;

    public WarningDTO() {}

    private WarningDTO(Builder b) {
        this.id = b.id; this.parkingLotId = b.parkingLotId;
        this.parkingLotName = b.parkingLotName; this.alertType = b.alertType;
        this.alertLevel = b.alertLevel; this.message = b.message;
        this.isResolved = b.isResolved; this.createdAt = b.createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private Long parkingLotId;
        private String parkingLotName; private String alertType;
        private String alertLevel; private String message;
        private Boolean isResolved; private LocalDateTime createdAt;
        public Builder id(Long v) { id = v; return this; }
        public Builder parkingLotId(Long v) { parkingLotId = v; return this; }
        public Builder parkingLotName(String v) { parkingLotName = v; return this; }
        public Builder alertType(String v) { alertType = v; return this; }
        public Builder alertLevel(String v) { alertLevel = v; return this; }
        public Builder message(String v) { message = v; return this; }
        public Builder isResolved(Boolean v) { isResolved = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public WarningDTO build() { return new WarningDTO(this); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
