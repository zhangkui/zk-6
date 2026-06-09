package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("prediction_records")
public class PredictionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private LocalDate predictionDate;

    private Integer hour;

    private BigDecimal predictedOccupancyRate;

    private Integer predictedInflow;

    private Integer predictedOutflow;

    private BigDecimal confidence;

    private String modelVersion;

    private LocalDateTime createdAt;
}
