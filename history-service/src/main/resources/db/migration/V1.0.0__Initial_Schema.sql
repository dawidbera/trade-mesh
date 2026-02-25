-- Enable TimescaleDB extension if available
DO $$ 
BEGIN
    CREATE EXTENSION IF NOT EXISTS timescaledb;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'TimescaleDB extension not available, falling back to standard PostgreSQL';
END $$;

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    assetId VARCHAR(50) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    side VARCHAR(10) NOT NULL
);

-- Convert to hypertable if extension is loaded
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
        PERFORM create_hypertable('transactions', 'timestamp', if_not_exists => TRUE);
    END IF;
END $$;

-- Create a continuous aggregate for OHLC if extension is loaded
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
        IF NOT EXISTS (SELECT 1 FROM pg_matviews WHERE matviewname = 'ohlc_1m') THEN
            EXECUTE 'CREATE MATERIALIZED VIEW ohlc_1m
                     WITH (timescaledb.continuous) AS
                     SELECT
                         time_bucket(''1 minute'', timestamp) AS bucket,
                         assetId,
                         first(price, timestamp) AS open,
                         max(price) AS high,
                         min(price) AS low,
                         last(price, timestamp) AS close,
                         sum(volume) AS volume
                     FROM transactions
                     GROUP BY bucket, assetId';
            
            PERFORM add_continuous_aggregate_policy('ohlc_1m',
                start_offset => INTERVAL '1 month',
                end_offset => INTERVAL '1 minute',
                schedule_interval => INTERVAL '1 minute',
                if_not_exists => TRUE);
        END IF;
    END IF;
END $$;
