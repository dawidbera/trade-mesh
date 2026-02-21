# market-data-service

Real-time market price simulator for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Simulation:** Periodically updates prices for a fixed set of assets (BTC, ETH, AAPL, etc.).
- **Live State:** Maintains current prices in **Redis**.
- **Distribution:** Publishes price updates as JSON events to **RabbitMQ** (Fanout exchange `market-prices`).
- **Internal API:** Provides gRPC `PriceService` for synchronous price retrieval.

## 📡 API & Messaging
- **gRPC Port:** `9001`
- **RabbitMQ Exchange:** `market-prices` (type: fanout)
- **Health:** `GET /health`

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
