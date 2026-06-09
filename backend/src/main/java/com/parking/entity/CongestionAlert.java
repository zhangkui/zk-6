package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("congestion_alerts")
public class CongestionAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private String alertType;

    private String alertLevel;

    private String message;

    private BigDecimal occupancyRate;

    private LocalDateTime predictedTime;

    private Boolean isResolved;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
