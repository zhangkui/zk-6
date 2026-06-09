package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkingLot;
import com.parking.dto.ParkingLotStatusDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ParkingLotMapper extends BaseMapper<ParkingLot> {

    @Select("SELECT p.*, " +
            "ROUND((p.total_spaces - p.available_spaces)::numeric / p.total_spaces * 100, 2) as occupancy_rate " +
            "FROM parking_lots p " +
            "ORDER BY occupancy_rate DESC")
    List<ParkingLotStatusDTO> selectAllStatus();

    @Select("SELECT p.*, " +
            "ROUND((p.total_spaces - p.available_spaces)::numeric / p.total_spaces * 100, 2) as occupancy_rate " +
            "FROM parking_lots p " +
            "WHERE p.id = #{id}")
    ParkingLotStatusDTO selectStatusById(Long id);
}
