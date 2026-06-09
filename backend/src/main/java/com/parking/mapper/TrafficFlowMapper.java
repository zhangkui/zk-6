package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.TrafficFlow;
import com.parking.dto.HourlyFlowDTO;
import com.parking.dto.DailyFlowDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TrafficFlowMapper extends BaseMapper<TrafficFlow> {

    @Select("SELECT hour, inflow, outflow, occupancy_rate " +
            "FROM traffic_flow " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND record_date = #{date} " +
            "ORDER BY hour")
    List<HourlyFlowDTO> selectHourlyFlowByDate(@Param("parkingLotId") Long parkingLotId,
                                               @Param("date") LocalDate date);

    @Select("SELECT hour, AVG(inflow) as inflow, AVG(outflow) as outflow, " +
            "AVG(occupancy_rate) as occupancy_rate " +
            "FROM traffic_flow " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY hour " +
            "ORDER BY hour")
    List<HourlyFlowDTO> selectAvgHourlyFlow(@Param("parkingLotId") Long parkingLotId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Select("SELECT record_date as date, SUM(inflow) as total_inflow, " +
            "SUM(outflow) as total_outflow, AVG(occupancy_rate) as avg_occupancy_rate " +
            "FROM traffic_flow " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date " +
            "ORDER BY record_date")
    List<DailyFlowDTO> selectDailyFlowRange(@Param("parkingLotId") Long parkingLotId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Select("SELECT hour, AVG(inflow) as inflow, AVG(outflow) as outflow, " +
            "AVG(occupancy_rate) as occupancy_rate " +
            "FROM traffic_flow " +
            "WHERE parking_lot_id = #{parkingLotId} " +
            "AND EXTRACT(DOW FROM record_date) = #{dayOfWeek} " +
            "AND record_date >= #{startDate} " +
            "GROUP BY hour " +
            "ORDER BY hour")
    List<HourlyFlowDTO> selectHistoricalPatternByDay(@Param("parkingLotId") Long parkingLotId,
                                                     @Param("dayOfWeek") Integer dayOfWeek,
                                                     @Param("startDate") LocalDate startDate);
}
