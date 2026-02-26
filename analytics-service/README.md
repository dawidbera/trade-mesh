# analytics-service

Technical indicators computation engine for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Data Consumption:** Asynchronously consumes live price ticks from **RabbitMQ**.
- **Computation:** Calculates technical indicators (e.g., Simple Moving Average) using a sliding window.
- **State Management:** Stores calculated indicators in **Redis** for quick gRPC retrieval.
- **Internal API:** Provides gRPC `AnalyticsService` for the Gateway.
- **Security:** Production secrets (RabbitMQ, Redis) are managed via **HashiCorp Vault** or direct **Environment Variables** in Sandbox.

## 🛠️ Semantic Warm-up
Implements a custom readiness check. The service reports `DOWN` until the **Redis** connection pool is fully initialized and a successful PING is executed.

## 📡 API & Messaging
- **gRPC Port:** `9002`
- **RabbitMQ Queue:** `analytics-price-queue` (bound to `market-prices` exchange)
- **Health:** `GET /q/health`

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
