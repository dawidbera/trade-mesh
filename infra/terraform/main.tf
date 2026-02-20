# main.tf — Core infrastructure for TradeMesh

locals {
  common_labels = {
    app       = var.project_name
    part-of   = var.project_name
    managed-by = "terraform"
  }
}

# Example: Defining a NetworkPolicy for the Sandbox (crucial for traffic control)
resource "kubernetes_network_policy" "default_deny_all" {
  metadata {
    name      = "default-deny-all"
    namespace = var.namespace
  }

  spec {
    pod_selector {}
    policy_types = ["Ingress"]
  }
}

# Allow internal traffic within the namespace (essential for gRPC and Redis)
resource "kubernetes_network_policy" "allow_intra_namespace" {
  metadata {
    name      = "allow-intra-namespace"
    namespace = var.namespace
  }

  spec {
    pod_selector {}
    ingress {
      from {
        pod_selector {}
      }
    }
    policy_types = ["Ingress"]
  }
}
