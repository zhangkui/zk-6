package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("traffic_flow")
public class TrafficFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private LocalDate recordDate;

    private Integer hour;

    private Integer inflow;

    private Integer outflow;

    private BigDecimal occupancyRate;

    private LocalDateTime createdAt;
}
