# gateway-service

The GraphQL API Gateway and Backend-for-Frontend (BFF) for the TradeMesh platform.

## 🚀 Key Responsibilities
- **Aggregation:** Collects data from `market`, `analytics`, and `history` services via gRPC.
- **Parallel Fetching:** Uses **Java 21 Virtual Threads** to aggregate data without blocking IO.
- **Real-time Streaming:** Consumes from RabbitMQ and pushes updates via **GraphQL Subscriptions (WebSockets)**.
- **Dynamic Selection:** Supports filtering by `assetId` (BTC, ETH, AAPL) for live chart updates.
- **Resilience:** Protects downstream calls with **Circuit Breakers** and **Fallbacks**.
- **Security:** Fully integrated with **Keycloak (OIDC)** and **HashiCorp Vault**.

## 📡 API Endpoints
- **GraphQL:** `POST /graphql`
- **GraphQL Subscription:** `WS /graphql`
- **GraphQL UI:** `GET /q/graphql-ui` (Enabled in Prod)
- **Health:** `GET /q/health` (includes Semantic Warm-up check)

## 🛠️ Semantic Warm-up
This service implements a custom readiness check. It reports `DOWN` until it has successfully established connectivity with downstream gRPC services through a series of probe calls.

## 💻 Development
Run in dev mode:
```shell script
./mvnw quarkus:dev
```
