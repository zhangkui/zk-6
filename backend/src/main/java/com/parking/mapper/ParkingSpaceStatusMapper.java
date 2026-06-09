package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkingSpaceStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ParkingSpaceStatusMapper extends BaseMapper<ParkingSpaceStatus> {

    @Select("SELECT * FROM parking_space_status " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "ORDER BY space_number")
    List<ParkingSpaceStatus> selectByParkingLotId(Long parkingLotId);

    @Select("SELECT COUNT(*) FROM parking_space_status " +
            "WHERE parking_lot_id = #{parkingLotId} AND is_occupied = #{isOccupied}")
    Long countByOccupied(@Param("parkingLotId") Long parkingLotId,
                         @Param("isOccupied") Boolean isOccupied);

    @Update("UPDATE parking_space_status " +
            "SET is_occupied = #{isOccupied}, last_updated = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isOccupied") Boolean isOccupied);
}
