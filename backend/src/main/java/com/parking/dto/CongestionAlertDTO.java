package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CongestionAlertDTO {

    private Long id;
    private Long parkingLotId;
    private String parkingLotName;
    private String alertType;
    private String alertLevel;
    private String message;
    private BigDecimal occupancyRate;
    private LocalDateTime predictedTime;
    private LocalDateTime createdAt;
    private Boolean isResolved;
}
