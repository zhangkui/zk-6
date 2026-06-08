CREATE TABLE IF NOT EXISTS parking_lot (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    total_spots INTEGER NOT NULL DEFAULT 0,
    available_spots INTEGER NOT NULL DEFAULT 0,
    district VARCHAR(100),
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parking_spot (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lot(id),
    spot_code VARCHAR(50) NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS traffic_record (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lot(id),
    record_date DATE NOT NULL,
    record_hour INTEGER NOT NULL,
    entry_count INTEGER DEFAULT 0,
    exit_count INTEGER DEFAULT 0,
    occupancy_rate DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prediction_result (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lot(id),
    predict_date DATE NOT NULL,
    predict_hour INTEGER NOT NULL,
    predicted_occupancy DECIMAL(5, 2) DEFAULT 0,
    heat_level VARCHAR(20),
    confidence DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS warning_alert (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT REFERENCES parking_lot(id),
    alert_type VARCHAR(50),
    alert_level VARCHAR(20),
    message VARCHAR(500),
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_traffic_record_lot_date ON traffic_record(parking_lot_id, record_date);
CREATE INDEX IF NOT EXISTS idx_prediction_result_lot_date ON prediction_result(parking_lot_id, predict_date);
CREATE INDEX IF NOT EXISTS idx_warning_alert_created ON warning_alert(created_at DESC);
