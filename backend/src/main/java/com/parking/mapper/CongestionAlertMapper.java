package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.CongestionAlert;
import com.parking.dto.CongestionAlertDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CongestionAlertMapper extends BaseMapper<CongestionAlert> {

    @Select("SELECT a.*, p.name as parking_lot_name " +
            "FROM congestion_alerts a " +
            "LEFT JOIN parking_lots p ON a.parking_lot_id = p.id " +
            "WHERE a.is_resolved = false " +
            "ORDER BY a.created_at DESC")
    List<CongestionAlertDTO> selectActiveAlerts();

    @Select("SELECT a.*, p.name as parking_lot_name " +
            "FROM congestion_alerts a " +
            "LEFT JOIN parking_lots p ON a.parking_lot_id = p.id " +
            "WHERE a.parking_lot_id = #{parkingLotId} " +
            "ORDER BY a.created_at DESC " +
            "LIMIT 20")
    List<CongestionAlertDTO> selectAlertsByParkingLot(Long parkingLotId);

    @Select("SELECT a.*, p.name as parking_lot_name " +
            "FROM congestion_alerts a " +
            "LEFT JOIN parking_lots p ON a.parking_lot_id = p.id " +
            "ORDER BY a.created_at DESC " +
            "LIMIT 50")
    List<CongestionAlertDTO> selectRecentAlerts();
}
