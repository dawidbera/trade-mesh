# iam.tf — Keycloak IAM configuration for TradeMesh

# Keycloak for OIDC/SAML authentication and RBAC
# Resource-optimized for Red Hat Sandbox
resource "helm_release" "keycloak" {
  name       = "keycloak"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "keycloak"
  namespace  = var.namespace

  set {
    name  = "fullnameOverride"
    value = "keycloak"
  }

  set {
    name  = "replicaCount"
    value = "1"
  }

  # Admin credentials (In production, move to Vault)
  set {
    name  = "auth.adminUser"
    value = "admin"
  }

  set {
    name  = "auth.adminPassword"
    value = "admin-secret"
  }

  # Database Integration (Using existing TimescaleDB to save 1GiB RAM)
  set {
    name  = "postgresql.enabled"
    value = "false"
  }

  set {
    name  = "externalDatabase.host"
    value = "timescaledb"
  }

  set {
    name  = "externalDatabase.user"
    value = "postgres"
  }

  set {
    name  = "externalDatabase.database"
    value = "trademesh"
  }

  set {
    name  = "externalDatabase.password"
    value = "trademesh-secret"
  }

  # Resources for Keycloak (Java/Quarkus based)
  set {
    name  = "resources.requests.memory"
    value = "1Gi"
  }

  set {
    name  = "resources.limits.memory"
    value = "2Gi"
  }

  # OpenShift specific settings (HTTP Proxy)
  set {
    name  = "proxy"
    value = "edge" # Suitable for OpenShift Routes
  }

  set {
    name  = "httpRelativePath"
    value = "/auth"
  }
}
