# history-service

Time-series archival and retrieval service for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Persistence:** Consumes live prices from **RabbitMQ** and archives them into **TimescaleDB** (PostgreSQL).
- **History Retrieval:** Provides historical OHLC (Open-High-Low-Close) data for charting.
- **Internal API:** Provides gRPC `HistoryService` for historical queries.

## 🛠️ Semantic Warm-up
Implements a custom readiness check. The service reports `DOWN` until the database connection pool is fully initialized and a successful "warm-up" SQL query (`SELECT 1`) is executed.

## 📡 API & Messaging
- **gRPC Port:** `9003`
- **RabbitMQ Queue:** `history-archive-queue` (bound to `market-prices` exchange)
- **Database:** PostgreSQL (optimized for TimescaleDB)
- **Health:** `GET /health`

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
