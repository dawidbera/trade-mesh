# messaging.tf — RabbitMQ configuration for TradeMesh

# RabbitMQ for gRPC service-to-service communication
# Resource-optimized for Red Hat Sandbox
resource "helm_release" "rabbitmq" {
  name       = "rabbitmq"
  repository = "https://charts.bitnami.com/bitnami"
  chart      = "rabbitmq"
  namespace  = var.namespace

  set {
    name  = "fullnameOverride"
    value = "rabbitmq"
  }

  set {
    name  = "replicaCount"
    value = "1" # Standalone for Sandbox efficiency
  }

  set {
    name  = "auth.username"
    value = "trademesh"
  }

  set {
    name  = "auth.password"
    value = "trademesh-secret" # In production, this should come from Vault
  }

  # Resources optimized for the 12GiB Data Layer allocation
  set {
    name  = "resources.requests.memory"
    value = "512Mi"
  }

  set {
    name  = "resources.limits.memory"
    value = "1Gi"
  }

  # Persistence for message durability
  set {
    name  = "persistence.size"
    value = "2Gi"
  }

  # Enable management plugin for easier debugging
  set {
    name  = "extraPlugins"
    value = "rabbitmq_management rabbitmq_auth_backend_cache"
  }
}
