# TradeMesh — High-Performance Financial Data Mesh

TradeMesh is a cloud-native, real-time financial data platform designed specifically for **Red Hat OpenShift**. It leverages the power of **gRPC** for high-speed internal communication and **GraphQL** for a flexible API gateway experience.

## 🚀 Key Features
- **Real-time gRPC Mesh:** Low-latency communication between microservices using server-side streaming.
- **GraphQL API Gateway:** Unified entry point with parallel data fetching using **Java 21 Virtual Threads**.
- **Asynchronous Event Bus:** Real-time price distribution using **RabbitMQ** (Reliable image via **Amazon Public ECR**).
- **Time-Series Persistence:** Optimized storage using **PostgreSQL** (Standard Aggregates + TimescaleDB support).
- **Self-Healing Infrastructure:** Advanced Kubernetes probes (**Semantic Warm-up**, **Deadlock Buster**).
- **Flexible Security:** Support for **HashiCorp Vault** secrets or direct **Environment Variable** injection for restricted Sandbox environments.
- **Modern UI:** Angular 21 Dashboard with real-time **Candlestick Charts** via Highcharts.

## 🏗️ Architecture & Request Flow (OpenShift Sandbox)

The system follows the **Backend-for-Frontend (BFF)** pattern with a high-speed gRPC mesh and asynchronous event bus:

```mermaid
graph TD
    subgraph "External World"
        Client[Angular 21 Client + Real-time Charts]
    end

    subgraph "Infrastructure (Helm)"
        RabbitMQ{RabbitMQ / market.prices}
        Redis[(Redis Cache)]
        Timescale[(PostgreSQL / TimescaleDB)]
    end

    subgraph "TradeMesh Gateway (BFF)"
        Gateway[GraphQL Gateway + Subscriptions]
    end

    subgraph "gRPC Mesh (Internal - Sync)"
        Market[Market Data Engine]
        Analytics[Analytics Service]
        History[History Service]
    end

    subgraph "Configuration Management"
        Env[Direct Environment Variables]
        Vault[HashiCorp Vault - Optional/Bypassed]
    end

    %% Request Flows
    Client -- "1. GraphQL Query / WS Sub" --> Gateway
    Gateway -- "2. Auth (Keycloak Bypassed in Sandbox)" .-> Vault
    
    %% Internal gRPC calls (Sync)
    Gateway -- "3. gRPC (Parallel OHLC + Price)" --> Market
    Gateway -- "3. gRPC (Parallel OHLC + Price)" --> Analytics
    Gateway -- "3. gRPC (Parallel OHLC + Price)" --> History

    %% Event Flow (Async)
    Market -- "4. Pub JSON" --> RabbitMQ
    RabbitMQ -- "5. Sub (AssetId Filtered)" --> Analytics
    RabbitMQ -- "5. Sub (AssetId Filtered)" --> History
    RabbitMQ -- "5. Dynamic WS Push" --> Gateway
    
    %% Storage
    Market -- "Store State" --> Redis
    Analytics -- "Indicators" --> Redis
    History -- "Archive + Aggregates" --> Timescale

    %% Configuration
    Env -- "Inject Credentials" --> Market
    Env -- "Inject Credentials" --> Analytics
    Env -- "Inject Credentials" --> History
    Env -- "Inject Credentials" --> Gateway
```

### Data Flow Lifecycle:
1. **Synchronous (BFF):** User loads the dashboard. Gateway fetches live data, indicators, and historical **OHLC** in parallel via gRPC using **Java 21 Virtual Threads**.
2. **Resilience:** Circuit Breakers trigger **Fallbacks** if backend services are slow or down.
3. **Asynchronous (Data Mesh):** Market Engine generates price ticks and broadcasts them to **RabbitMQ**.
4. **Real-time:** Gateway consumes RabbitMQ events and pushes them to the client via **GraphQL Subscriptions (WebSockets)**.
5. **Security:** Secrets are managed via **Vault** or direct **Env Vars** depending on the environment.
6. **Self-Healing:** **Database Deadlock Buster** and **Semantic Warm-up** ensure high availability.


## 🧪 Testing
To run tests across all services (requires Docker for DevServices):
```bash
# In headless environments (CLI/CI), use xvfb-run:
xvfb-run ./run_tests.sh
```

## 🚢 Deployment to Red Hat OpenShift
To deploy the entire stack automatically to your Red Hat Sandbox:

### 1. Backend & Infrastructure
```bash
./deploy_to_openshift.sh
```
This script provisions infrastructure via Helm and triggers **Binary S2I builds** for all backend microservices.

### 2. Frontend (Angular)
```bash
./deploy_frontend.sh
```
This script builds the Angular app locally and deploys it to OpenShift as a static site powered by **Nginx**.

## 🔗 Environment Access (Sandbox)
- **Frontend:** `http://frontend-[namespace].apps.rm2.thpm.p1.openshiftapps.com`
- **GraphQL UI:** `http://gateway-service-[namespace].apps.rm2.thpm.p1.openshiftapps.com/q/graphql-ui`
- **Health Checks:** `http://[service-name]-[namespace].apps.rm2.thpm.p1.openshiftapps.com/q/health`
