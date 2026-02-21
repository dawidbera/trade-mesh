# security.tf — HashiCorp Vault for TradeMesh Secrets Management

# Vault for centralized secrets and dynamic DB credentials
# Resource-optimized for Red Hat Sandbox (Part of the 4GiB Security allocation)
resource "helm_release" "vault" {
  name       = "vault"
  repository = "https://hashicorp.github.io/helm"
  chart      = "vault"
  namespace  = var.namespace

  # Standalone mode for efficiency in Sandbox
  set {
    name  = "server.standalone.enabled"
    value = "true"
  }

  # Enable UI for easier management during development
  set {
    name  = "ui.enabled"
    value = "true"
  }

  set {
    name  = "server.resources.requests.memory"
    value = "512Mi"
  }

  set {
    name  = "server.resources.limits.memory"
    value = "1Gi"
  }

  # Use a small PVC for persistent storage of the encrypted vault
  set {
    name  = "server.dataStorage.size"
    value = "2Gi"
  }

  # Development mode (auto-unseal, in-memory) is tempting, 
  # but we want persistence for M2/M3 milestones.
  # If you prefer a faster setup without manual unsealing, 
  # we could switch to 'server.dev.enabled=true'.
  set {
    name  = "server.dev.enabled"
    value = "false"
  }

  # Post-install: We will need to initialize and unseal Vault manually 
  # via 'kubectl exec' once it's deployed.
}
