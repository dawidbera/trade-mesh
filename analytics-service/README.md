# analytics-service

Technical indicators computation engine for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Data Consumption:** Asynchronously consumes live price ticks from **RabbitMQ**.
- **Computation:** Calculates technical indicators (e.g., Simple Moving Average) using a sliding window.
- **State Management:** Stores calculated indicators in **Redis** for quick gRPC retrieval.
- **Internal API:** Provides gRPC `AnalyticsService` for the Gateway.

## 📡 API & Messaging
- **gRPC Port:** `9002`
- **RabbitMQ Queue:** `analytics-price-queue` (bound to `market-prices` exchange)
- **Health:** `GET /health`

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
