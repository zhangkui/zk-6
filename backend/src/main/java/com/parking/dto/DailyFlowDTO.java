package com.parking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyFlowDTO {

    private LocalDate date;
    private Integer totalInflow;
    private Integer totalOutflow;
    private java.math.BigDecimal avgOccupancyRate;
}
