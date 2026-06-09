package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PredictionResultDTO {

    private LocalDate predictionDate;
    private Integer hour;
    private BigDecimal predictedOccupancyRate;
    private Integer predictedInflow;
    private Integer predictedOutflow;
    private BigDecimal confidence;
    private String heatLevel;
}
