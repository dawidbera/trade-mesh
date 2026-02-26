# history-service

Time-series archival and retrieval service for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Persistence:** Consumes live prices from **RabbitMQ** and archives them into **TimescaleDB** (Postgres).
- **Time-series Optimization:**
  - **Hypertables:** Partitioned by `timestamp` for high-performance ingestion.
  - **OHLC Aggregates:** Automated 1-minute continuous aggregates (Open-High-Low-Close).
- **History Retrieval:** Provides real historical OHLC data for candlestick charting.
- **Internal API:** gRPC `HistoryService` for historical queries.

## 🛠️ Semantic Warm-up
Implements a custom readiness check. The service reports `DOWN` until the database connection pool is fully initialized and a successful "warm-up" SQL query (`SELECT 1`) is executed.

## 🛡️ Database Deadlock Buster
A custom liveness health check that monitors the **Agroal connection pool**. If pool exhaustion or deadlock is detected, it signals Kubernetes to restart the Pod, ensuring high availability.

## 📡 API & Messaging
- **gRPC Port:** `9003`
- **RabbitMQ Queue:** `history-archive-queue` (bound to `market-prices` exchange)
- **Database:** PostgreSQL (optimized for TimescaleDB)
- **Health:** `GET /q/health` (Readiness + Liveness)

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
