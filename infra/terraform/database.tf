# database.tf — TimescaleDB and Redis for TradeMesh

# TimescaleDB (PostgreSQL-based) via Bitnami Chart
# Resource-optimized for Red Hat Sandbox
resource "helm_release" "timescaledb" {
  name       = "timescaledb"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "postgresql"
  namespace  = var.namespace

  set {
    name  = "fullnameOverride"
    value = "timescaledb"
  }

  set {
    name  = "image.repository"
    value = "timescale/timescaledb"
  }

  set {
    name  = "image.tag"
    value = "latest-pg16" # Using PG16 with TimescaleDB
  }

  set {
    name  = "auth.database"
    value = "trademesh"
  }

  set {
    name  = "primary.persistence.size"
    value = "10Gi" # Fits well within Sandbox 25Gi limit
  }

  set {
    name  = "primary.resources.requests.memory"
    value = "1Gi"
  }

  set {
    name  = "primary.resources.limits.memory"
    value = "2Gi"
  }
}

# Redis for Market Data caching
resource "helm_release" "redis" {
  name       = "redis"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "redis"
  namespace  = var.namespace

  set {
    name  = "architecture"
    value = "standalone" # Minimalistic for Sandbox
  }

  set {
    name  = "master.resources.requests.memory"
    value = "256Mi"
  }

  set {
    name  = "master.resources.limits.memory"
    value = "512Mi"
  }
}
