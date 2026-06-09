package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("parking_lots")
public class ParkingLot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private Integer totalSpaces;

    private Integer availableSpaces;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String district;

    private String type;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
