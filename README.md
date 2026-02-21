# TradeMesh Technical Documentation

This directory contains the core implementation of the **TradeMesh** platform, including infrastructure, gRPC contracts, and microservices.

## 🏗️ Core Infrastructure (Terraform)
Located in `infra/terraform/`, these files manage the complete foundation for the Red Hat Sandbox environment.

- `database.tf`: Provisions **TimescaleDB** (PostgreSQL) and **Redis**.
- `messaging.tf`: Provisions **RabbitMQ** for service-to-service communication.
- `security.tf`: Provisions **HashiCorp Vault** for secrets management.
- `iam.tf`: Provisions **Keycloak** (IAM) for authentication.
- `gitops.tf`: Provisions **ArgoCD** (OpenShift GitOps) for CD.
- `main.tf`: Configures **NetworkPolicies** for gRPC traffic isolation.

### Deployment Guide
1. Configure `terraform.tfvars` with your sandbox namespace (e.g., `dawidbera-dev`).
2. `terraform init`
3. `terraform apply`

---

## 📡 gRPC Mesh Contracts
Located in `proto/`, these files define the "Source of Truth" for all communications.

- **`market.proto`**: Real-time market prices (Streaming).
- **`analytics.proto`**: Technical indicators (RSI, MA).
- **`history.proto`**: Historical data and OHLC Candlestick series.

### Code Generation (Java/Quarkus)
To generate the gRPC client and server code, use the `quarkus-grpc` extension. Quarkus will automatically detect the `.proto` files in the `src/main/proto` or a linked directory.

---

## 🔐 Security Configuration
### HashiCorp Vault
After deployment, initialize and unseal Vault manually:
```bash
kubectl exec -it vault-0 -- vault operator init
kubectl exec -it vault-0 -- vault operator unseal <KEY>
```

### Keycloak
Default credentials (configured in `iam.tf`):
- **Admin User:** `admin`
- **Admin Password:** `admin-secret`

---

## 📂 Project Organization
- `infra/terraform/`: All IaC files.
- `proto/`: Shared gRPC definitions.
- `services/`: (Upcoming) Java 21 Quarkus microservices.
- `gateway/`: (Upcoming) GraphQL Gateway implementation.
