package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.PredictionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PredictionRecordMapper extends BaseMapper<PredictionRecord> {

    @Select("SELECT * FROM prediction_records " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND prediction_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY prediction_date, hour")
    List<PredictionRecord> selectByParkingLotAndDateRange(@Param("parkingLotId") Long parkingLotId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM prediction_records " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND prediction_date = #{date} " +
            "ORDER BY hour")
    List<PredictionRecord> selectByParkingLotAndDate(@Param("parkingLotId") Long parkingLotId,
                                                     @Param("date") LocalDate date);

    @Select("DELETE FROM prediction_records " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND prediction_date = #{date}")
    void deleteByParkingLotAndDate(@Param("parkingLotId") Long parkingLotId,
                                   @Param("date") LocalDate date);
}
