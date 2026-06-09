package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkingLotStatusDTO {

    private Long id;
    private String name;
    private String address;
    private Integer totalSpaces;
    private Integer availableSpaces;
    private Integer occupiedSpaces;
    private BigDecimal occupancyRate;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String district;
    private String type;
    private String status;
    private String congestionLevel;
}
