package com.parking.dto;

import lombok.Data;

@Data
public class HourlyFlowDTO {

    private Integer hour;
    private Integer inflow;
    private Integer outflow;
    private java.math.BigDecimal occupancyRate;
}
