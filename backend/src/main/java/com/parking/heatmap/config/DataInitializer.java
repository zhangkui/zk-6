package com.parking.heatmap.config;

import com.parking.heatmap.entity.ParkingLot;
import com.parking.heatmap.entity.ParkingSpot;
import com.parking.heatmap.entity.TrafficRecord;
import com.parking.heatmap.prediction.PredictionService;
import com.parking.heatmap.repository.ParkingLotRepository;
import com.parking.heatmap.repository.ParkingSpotRepository;
import com.parking.heatmap.repository.TrafficRecordRepository;
import com.parking.heatmap.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final TrafficRecordRepository trafficRecordRepository;
    private final PredictionService predictionService;

    private final Random random = new Random(42);

    private static final List<Object[]> PARKING_LOT_DATA = Arrays.asList(
            new Object[]{"国贸中心停车场", "朝阳区建国门外大街1号", new BigDecimal("39.9087"), new BigDecimal("116.4605"), 500, "朝阳区"},
            new Object[]{"王府井百货停车场", "东城区王府井大街255号", new BigDecimal("39.9154"), new BigDecimal("116.4108"), 300, "东城区"},
            new Object[]{"西单大悦城停车场", "西城区西单北大街131号", new BigDecimal("39.9105"), new BigDecimal("116.3743"), 400, "西城区"},
            new Object[]{"中关村广场停车场", "海淀区中关村大街15号", new BigDecimal("39.9836"), new BigDecimal("116.3169"), 600, "海淀区"},
            new Object[]{"三里屯太古里停车场", "朝阳区三里屯路19号", new BigDecimal("39.9372"), new BigDecimal("116.4543"), 350, "朝阳区"},
            new Object[]{"望京SOHO停车场", "朝阳区阜通东大街1号", new BigDecimal("39.9924"), new BigDecimal("116.4782"), 450, "朝阳区"},
            new Object[]{"金融街购物中心停车场", "西城区金城坊街2号", new BigDecimal("39.9165"), new BigDecimal("116.3612"), 380, "西城区"},
            new Object[]{"蓝色港湾停车场", "朝阳区朝阳公园路6号", new BigDecimal("39.9448"), new BigDecimal("116.4786"), 520, "朝阳区"},
            new Object[]{"金源新燕莎MALL停车场", "海淀区远大路1号", new BigDecimal("39.9589"), new BigDecimal("116.2864"), 800, "海淀区"},
            new Object[]{"崇文门新世界停车场", "东城区崇文门外大街3号", new BigDecimal("39.8971"), new BigDecimal("116.4176"), 280, "东城区"}
    );

    public DataInitializer(ParkingLotRepository parkingLotRepository, ParkingSpotRepository parkingSpotRepository,
                           TrafficRecordRepository trafficRecordRepository, PredictionService predictionService) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.trafficRecordRepository = trafficRecordRepository;
        this.predictionService = predictionService;
    }

    @Override
    public void run(String... args) {
        if (parkingLotRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            ensurePredictionsExist();
            return;
        }

        log.info("开始初始化数据...");
        initializeParkingLots();
        initializeParkingSpots();
        initializeTrafficRecords();
        generatePredictions();
        log.info("数据初始化完成！");
    }

    private void initializeParkingLots() {
        log.info("初始化停车场数据...");
        for (Object[] data : PARKING_LOT_DATA) {
            ParkingLot lot = new ParkingLot();
            lot.setName((String) data[0]);
            lot.setAddress((String) data[1]);
            lot.setLatitude((BigDecimal) data[2]);
            lot.setLongitude((BigDecimal) data[3]);
            lot.setTotalSpots((Integer) data[4]);
            lot.setDistrict((String) data[5]);
            lot.setStatus(ParkingLot.Status.OPEN);
            int available = (int) (lot.getTotalSpots() * (0.1 + random.nextDouble() * 0.4));
            lot.setAvailableSpots(available);
            parkingLotRepository.save(lot);
        }
    }

    private void initializeParkingSpots() {
        log.info("初始化车位数据...");
        List<ParkingLot> lots = parkingLotRepository.findAll();
        List<ParkingSpot> spots = new ArrayList<>();
        for (ParkingLot lot : lots) {
            for (int i = 1; i <= Math.min(lot.getTotalSpots(), 50); i++) {
                ParkingSpot spot = new ParkingSpot();
                spot.setParkingLotId(lot.getId());
                spot.setSpotCode(String.format("A-%03d", i));
                spot.setIsOccupied(random.nextDouble() > 0.3);
                spots.add(spot);
            }
        }
        parkingSpotRepository.saveAll(spots);
    }

    private void initializeTrafficRecords() {
        log.info("初始化历史车流数据...");
        List<ParkingLot> lots = parkingLotRepository.findAll();
        List<LocalDate> dates = DateUtils.getLastNDays(30);
        List<TrafficRecord> allRecords = new ArrayList<>();

        for (ParkingLot lot : lots) {
            for (LocalDate date : dates) {
                for (int hour = 0; hour < 24; hour++) {
                    TrafficRecord record = generateTrafficRecord(lot, date, hour);
                    allRecords.add(record);
                }
            }
        }

        trafficRecordRepository.saveAll(allRecords);
    }

    private TrafficRecord generateTrafficRecord(ParkingLot lot, LocalDate date, int hour) {
        TrafficRecord record = new TrafficRecord();
        record.setParkingLotId(lot.getId());
        record.setRecordDate(date);
        record.setRecordHour(hour);

        boolean isWeekend = DateUtils.isWeekend(date);
        double baseOccupancy = getBaseOccupancy(hour, isWeekend);
        double noise = (random.nextDouble() - 0.5) * 15;
        double occupancy = Math.max(0, Math.min(100, baseOccupancy + noise));

        record.setOccupancyRate(BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP));

        int baseFlow = (int) (lot.getTotalSpots() * occupancy / 100 * 0.3);
        int entryVariation = random.nextInt(baseFlow / 2 + 1) - baseFlow / 4;
        int exitVariation = random.nextInt(baseFlow / 2 + 1) - baseFlow / 4;

        record.setEntryCount(Math.max(0, baseFlow + entryVariation));
        record.setExitCount(Math.max(0, baseFlow + exitVariation));

        return record;
    }

    private double getBaseOccupancy(int hour, boolean isWeekend) {
        if (hour >= 0 && hour < 6) {
            return 10 + random.nextDouble() * 15;
        } else if (hour >= 6 && hour < 9) {
            return isWeekend ? 35 + random.nextDouble() * 20 : 60 + random.nextDouble() * 25;
        } else if (hour >= 9 && hour < 12) {
            return isWeekend ? 70 + random.nextDouble() * 20 : 55 + random.nextDouble() * 15;
        } else if (hour >= 12 && hour < 14) {
            return isWeekend ? 80 + random.nextDouble() * 15 : 50 + random.nextDouble() * 15;
        } else if (hour >= 14 && hour < 17) {
            return isWeekend ? 75 + random.nextDouble() * 20 : 52 + random.nextDouble() * 15;
        } else if (hour >= 17 && hour < 20) {
            return isWeekend ? 65 + random.nextDouble() * 20 : 68 + random.nextDouble() * 22;
        } else {
            return 25 + random.nextDouble() * 20;
        }
    }

    private void generatePredictions() {
        log.info("生成预测数据...");
        List<Long> lotIds = parkingLotRepository.findAll().stream()
                .map(ParkingLot::getId)
                .toList();
        predictionService.generatePredictionsForAllLots(lotIds, 7);
    }

    private void ensurePredictionsExist() {
        long count = parkingLotRepository.count();
        if (count > 0) {
            List<Long> lotIds = parkingLotRepository.findAll().stream()
                    .map(ParkingLot::getId)
                    .toList();
            predictionService.generatePredictionsForAllLots(lotIds, 7);
        }
    }
}
