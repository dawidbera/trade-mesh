# gitops.tf — ArgoCD (OpenShift GitOps) configuration for TradeMesh

# ArgoCD for GitOps-based CD (Continuous Delivery)
# Resource-optimized for Red Hat Sandbox
resource "helm_release" "argocd" {
  name       = "argocd"
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argo-cd"
  namespace  = var.namespace

  set {
    name  = "fullnameOverride"
    value = "argocd"
  }

  # Minimalistic setup: Disable HA and high-resource components
  set {
    name  = "global.image.tag"
    value = "v2.10.4"
  }

  set {
    name  = "controller.replicas"
    value = "1"
  }

  set {
    name  = "repoServer.replicas"
    value = "1"
  }

  set {
    name  = "server.replicas"
    value = "1"
  }

  # Resource limits (Optimized for Sandbox)
  set {
    name  = "controller.resources.requests.memory"
    value = "256Mi"
  }

  set {
    name  = "controller.resources.limits.memory"
    value = "512Mi"
  }

  set {
    name  = "server.resources.requests.memory"
    value = "128Mi"
  }

  set {
    name  = "server.resources.limits.memory"
    value = "256Mi"
  }

  set {
    name  = "repoServer.resources.requests.memory"
    value = "128Mi"
  }

  set {
    name  = "repoServer.resources.limits.memory"
    value = "256Mi"
  }

  # OpenShift specific: Disable some features that require ClusterAdmin
  set {
    name  = "server.service.type"
    value = "ClusterIP"
  }

  # Enable UI and insecure access for testing (OpenShift Route will handle TLS)
  set {
    name  = "server.extraArgs"
    value = "{--insecure}"
  }
}

# (Optional) OpenShift Route for ArgoCD UI
# This requires the 'kubernetes' provider to support custom resources or using kubernetes_manifest
# For now, we rely on the Helm chart to provide basic connectivity.
