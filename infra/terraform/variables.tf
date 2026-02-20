# variables.tf — Sandbox-specific variables

variable "namespace" {
  type        = string
  description = "The Red Hat Developer Sandbox namespace (e.g., user-name-dev)"
  # No default — you'll need to provide it from your sandbox environment
}

variable "project_name" {
  type    = string
  default = "trade-mesh"
}
