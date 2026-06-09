CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS parking_lots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    total_spaces INT NOT NULL,
    available_spaces INT DEFAULT 0,
    longitude DECIMAL(10, 7) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    district VARCHAR(50),
    type VARCHAR(20) DEFAULT 'public',
    status VARCHAR(20) DEFAULT 'normal',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS traffic_flow (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    record_date DATE NOT NULL,
    hour INT NOT NULL,
    inflow INT DEFAULT 0,
    outflow INT DEFAULT 0,
    occupancy_rate DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS congestion_alerts (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    alert_type VARCHAR(20) NOT NULL,
    alert_level VARCHAR(20) NOT NULL,
    message VARCHAR(255),
    occupancy_rate DECIMAL(5, 2),
    predicted_time TIMESTAMP,
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prediction_records (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    prediction_date DATE NOT NULL,
    hour INT NOT NULL,
    predicted_occupancy_rate DECIMAL(5, 2),
    predicted_inflow INT,
    predicted_outflow INT,
    confidence DECIMAL(5, 2),
    model_version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parking_space_status (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    space_number VARCHAR(20) NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(parking_lot_id, space_number)
);

CREATE INDEX IF NOT EXISTS idx_traffic_flow_date ON traffic_flow(record_date, hour);
CREATE INDEX IF NOT EXISTS idx_traffic_flow_parking ON traffic_flow(parking_lot_id, record_date);
CREATE INDEX IF NOT EXISTS idx_prediction_date ON prediction_records(prediction_date, hour);
CREATE INDEX IF NOT EXISTS idx_congestion_active ON congestion_alerts(is_resolved, created_at);
CREATE INDEX IF NOT EXISTS idx_parking_status ON parking_space_status(parking_lot_id, is_occupied);

INSERT INTO parking_lots (name, address, total_spaces, available_spaces, longitude, latitude, district, type, status) VALUES
('市中心停车场', '市中心广场地下', 500, 125, 116.397, 39.908, '东城区', 'commercial', 'normal'),
('国贸停车场', '国贸大厦B1层', 800, 320, 116.460, 39.909, '朝阳区', 'commercial', 'normal'),
('科技园停车场', '科技园北区', 300, 85, 116.295, 39.985, '海淀区', 'tech', 'normal'),
('火车站停车场', '高铁站西广场', 600, 180, 116.415, 39.865, '丰台区', 'transport', 'normal'),
('购物中心停车场', '大悦城地下', 400, 95, 116.445, 39.925, '朝阳区', 'commercial', 'normal'),
('医院停车场', '人民医院门诊楼', 250, 45, 116.365, 39.915, '西城区', 'hospital', 'normal'),
('体育中心停车场', '奥林匹克公园', 1000, 650, 116.395, 39.995, '朝阳区', 'public', 'normal'),
('大学城停车场', '大学校区东门', 350, 175, 116.305, 39.975, '海淀区', 'education', 'normal');

DO $$
DECLARE
    pl_id INT;
    cur_date DATE;
    h INT;
    base_occupancy DECIMAL(5,2);
    inflow_val INT;
    outflow_val INT;
    occ_rate DECIMAL(5,2);
BEGIN
    FOR pl_id IN SELECT id FROM parking_lots LOOP
        FOR day_offset IN 0..29 LOOP
            cur_date := CURRENT_DATE - day_offset;
            
            FOR h IN 0..23 LOOP
                base_occupancy := 30 + 
                    CASE 
                        WHEN h BETWEEN 7 AND 9 THEN 35
                        WHEN h BETWEEN 11 AND 13 THEN 25
                        WHEN h BETWEEN 17 AND 20 THEN 40
                        WHEN h BETWEEN 21 AND 23 THEN 15
                        ELSE 10
                    END;
                
                inflow_val := FLOOR(base_occupancy * (0.8 + RANDOM() * 0.4));
                outflow_val := FLOOR(base_occupancy * (0.7 + RANDOM() * 0.4));
                occ_rate := base_occupancy + (RANDOM() * 20 - 10);
                
                IF occ_rate > 95 THEN occ_rate := 95; END IF;
                IF occ_rate < 10 THEN occ_rate := 10; END IF;
                
                INSERT INTO traffic_flow (parking_lot_id, record_date, hour, inflow, outflow, occupancy_rate)
                VALUES (pl_id, cur_date, h, inflow_val, outflow_val, ROUND(occ_rate, 2));
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

DO $$
DECLARE
    pl_id INT;
    space_num INT;
    total INT;
    occupied INT;
    is_occ BOOLEAN;
BEGIN
    FOR pl_id IN SELECT id FROM parking_lots LOOP
        SELECT total_spaces INTO total FROM parking_lots WHERE id = pl_id;
        SELECT available_spaces INTO occupied FROM parking_lots WHERE id = pl_id;
        occupied := total - occupied;
        
        FOR space_num IN 1..total LOOP
            IF space_num <= occupied THEN
                is_occ := TRUE;
            ELSE
                is_occ := FALSE;
            END IF;
            
            INSERT INTO parking_space_status (parking_lot_id, space_number, is_occupied)
            VALUES (pl_id, 'A' || LPAD(space_num::TEXT, 4, '0'), is_occ);
        END LOOP;
    END LOOP;
END $$;

INSERT INTO congestion_alerts (parking_lot_id, alert_type, alert_level, message, occupancy_rate, predicted_time) VALUES
(1, 'prediction', 'high', '预计今日18:00-20:00停车场将出现严重拥堵', 92.5, CURRENT_DATE + INTERVAL '18 hours'),
(2, 'real_time', 'medium', '当前停车场使用率已超过80%', 83.2, CURRENT_TIMESTAMP),
(6, 'real_time', 'high', '医院停车场即将满位，建议绕行', 94.8, CURRENT_TIMESTAMP),
(4, 'prediction', 'medium', '明日早高峰7:30-9:00预计拥堵', 78.5, CURRENT_DATE + INTERVAL '1 day 7.5 hours');
