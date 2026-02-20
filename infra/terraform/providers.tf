# providers.tf — Terraform configuration for Red Hat Developer Sandbox

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.30"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.13"
    }
  }
}

# The kubernetes provider will automatically use the current 'oc login' context
provider "kubernetes" {
  config_path = "~/.kube/config"
}

provider "helm" {
  kubernetes {
    config_path = "~/.kube/config"
  }
}

# Data source to fetch the current namespace from your sandbox context
data "kubernetes_namespace" "sandbox" {
  metadata {
    name = var.namespace
  }
}
