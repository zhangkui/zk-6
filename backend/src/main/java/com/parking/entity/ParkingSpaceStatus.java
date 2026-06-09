package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parking_space_status")
public class ParkingSpaceStatus {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private String spaceNumber;

    private Boolean isOccupied;

    private LocalDateTime lastUpdated;
}
