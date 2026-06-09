package com.parking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.dto.ParkingLotStatusDTO;
import com.parking.entity.ParkingLot;
import com.parking.mapper.ParkingLotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ParkingLotService {

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    public List<ParkingLot> list() {
        return parkingLotMapper.selectList(null);
    }

    public ParkingLot getById(Long id) {
        return parkingLotMapper.selectById(id);
    }

    public List<ParkingLotStatusDTO> getAllStatus() {
        List<ParkingLotStatusDTO> list = parkingLotMapper.selectAllStatus();
        for (ParkingLotStatusDTO dto : list) {
            dto.setOccupiedSpaces(dto.getTotalSpaces() - dto.getAvailableSpaces());
            if (dto.getOccupancyRate() == null) {
                BigDecimal rate = BigDecimal.valueOf(dto.getOccupiedSpaces())
                        .divide(BigDecimal.valueOf(dto.getTotalSpaces()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dto.setOccupancyRate(rate);
            }
            dto.setCongestionLevel(getCongestionLevel(dto.getOccupancyRate()));
        }
        return list;
    }

    public ParkingLotStatusDTO getStatusById(Long id) {
        ParkingLotStatusDTO dto = parkingLotMapper.selectStatusById(id);
        if (dto != null) {
            dto.setOccupiedSpaces(dto.getTotalSpaces() - dto.getAvailableSpaces());
            if (dto.getOccupancyRate() == null) {
                BigDecimal rate = BigDecimal.valueOf(dto.getOccupiedSpaces())
                        .divide(BigDecimal.valueOf(dto.getTotalSpaces()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                dto.setOccupancyRate(rate);
            }
            dto.setCongestionLevel(getCongestionLevel(dto.getOccupancyRate()));
        }
        return dto;
    }

    public ParkingLot updateAvailableSpaces(Long id, int availableSpaces) {
        ParkingLot parkingLot = parkingLotMapper.selectById(id);
        if (parkingLot == null) {
            return null;
        }
        parkingLot.setAvailableSpaces(availableSpaces);
        parkingLotMapper.updateById(parkingLot);
        return parkingLot;
    }

    private String getCongestionLevel(BigDecimal occupancyRate) {
        if (occupancyRate == null) return "normal";

        if (occupancyRate.compareTo(new BigDecimal("90")) >= 0) {
            return "critical";
        } else if (occupancyRate.compareTo(new BigDecimal("75")) >= 0) {
            return "warning";
        } else if (occupancyRate.compareTo(new BigDecimal("50")) >= 0) {
            return "moderate";
        } else {
            return "normal";
        }
    }

    public List<ParkingLot> getByDistrict(String district) {
        LambdaQueryWrapper<ParkingLot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingLot::getDistrict, district);
        return parkingLotMapper.selectList(wrapper);
    }
}
