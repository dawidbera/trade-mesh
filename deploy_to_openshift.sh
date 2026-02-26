#!/bin/bash

# --- TradeMesh OpenShift Deployment Automator ---
# Purpose: Fully deploy the TradeMesh stack to Red Hat OpenShift Sandbox.

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Calculate script location to support execution from any directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"

echo -e "${BLUE}=== TradeMesh OpenShift Deployer ===${NC}"

# 1. Connectivity Check
echo -e "${YELLOW}Checking OpenShift connectivity...${NC}"
if ! oc whoami &> /dev/null; then
    echo -e "${RED}Error: Not logged into OpenShift. Please run 'oc login' first.${NC}"
    exit 1
fi

NAMESPACE=$(oc project -q)
echo -e "${GREEN}Deploying to namespace: ${BLUE}$NAMESPACE${NC}"

# 1.2 Cleanup old failed/completed resources
echo -e "${YELLOW}Cleaning up old resources...${NC}"
oc delete pods --field-selector=status.phase=Succeeded &> /dev/null
oc delete pods --field-selector=status.phase=Failed &> /dev/null
oc delete jobs --all &> /dev/null
oc delete rs --all &> /dev/null
oc delete builds --all &> /dev/null

# 2. Infrastructure Layer
echo -e "${YELLOW}Deploying Infrastructure...${NC}"

helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

# 2.1 TimescaleDB
echo -e "${BLUE}Deploying TimescaleDB...${NC}"
oc delete statefulset timescaledb --ignore-not-found=true &> /dev/null
helm upgrade --install timescaledb bitnami/postgresql \
    --namespace "$NAMESPACE" \
    --set global.security.allowInsecureImages=true \
    --set fullnameOverride=timescaledb \
    --set image.repository=timescale/timescaledb \
    --set image.tag=latest-pg16 \
    --set auth.database=trademesh \
    --set auth.postgresPassword=trademesh-secret \
    --set auth.username=trademesh \
    --set auth.password=trademesh-secret \
    --set primary.persistence.enabled=false \
    --set primary.resources.requests.memory=256Mi \
    --set primary.resources.limits.memory=512Mi \
    --set primary.containerSecurityContext.enabled=false \
    --set primary.podSecurityContext.enabled=false \
    --set volumePermissions.enabled=false

# 2.2 Redis
echo -e "${BLUE}Deploying Redis...${NC}"
oc delete statefulset redis-master --ignore-not-found=true &> /dev/null
helm upgrade --install redis bitnami/redis \
    --namespace "$NAMESPACE" \
    --set architecture=standalone \
    --set auth.enabled=false \
    --set master.persistence.enabled=false \
    --set master.resources.requests.memory=128Mi \
    --set master.resources.limits.memory=256Mi \
    --set master.containerSecurityContext.enabled=false \
    --set master.podSecurityContext.enabled=false \
    --set volumePermissions.enabled=false

# 2.3 RabbitMQ
echo -e "${BLUE}Deploying RabbitMQ...${NC}"
# Uninstall first to ensure clean state
helm uninstall rabbitmq &> /dev/null
oc delete statefulset rabbitmq --ignore-not-found=true &> /dev/null
helm upgrade --install rabbitmq bitnami/rabbitmq \
    --namespace "$NAMESPACE" \
    --set global.security.allowInsecureImages=true \
    --set fullnameOverride=rabbitmq \
    --set image.registry=public.ecr.aws \
    --set image.repository=bitnami/rabbitmq \
    --set image.tag=latest \
    --set replicaCount=1 \
    --set auth.username=trademesh \
    --set auth.password=trademesh-secret \
    --set persistence.enabled=false \
    --set resources.requests.memory=256Mi \
    --set resources.limits.memory=512Mi \
    --set extraPlugins="rabbitmq_management" \
    --set containerSecurityContext.enabled=false \
    --set podSecurityContext.enabled=false \
    --set volumePermissions.enabled=false \
    --wait

# 3. Backend Services
SERVICES=(
    "market-data-service"
    "analytics-service"
    "history-service"
    "gateway-service"
)

echo -e "${YELLOW}Building and Deploying Backend Services...${NC}"
for service in "${SERVICES[@]}"; do
    echo -e "${BLUE}Deploying $service...${NC}"
    cd "$PROJECT_ROOT/$service" || exit
    
    # Inject env vars during build to ensure they are in the generated manifests
    ./mvnw clean package \
        -DskipTests \
        -Dquarkus.container-image.group="$NAMESPACE" \
        -Dquarkus.kubernetes.deploy=true \
        -Dquarkus.openshift.route.expose=true \
        -Dquarkus.kubernetes.revision-history-limit=1 \
        -Dquarkus.kubernetes.env.vars.QUARKUS_DATASOURCE_USERNAME=trademesh \
        -Dquarkus.kubernetes.env.vars.QUARKUS_DATASOURCE_PASSWORD=trademesh-secret \
        -Dquarkus.kubernetes.env.vars.MP_MESSAGING_CONNECTOR_SMALLRYE_RABBITMQ_USERNAME=trademesh \
        -Dquarkus.kubernetes.env.vars.MP_MESSAGING_CONNECTOR_SMALLRYE_RABBITMQ_PASSWORD=trademesh-secret \
        -Dquarkus.kubernetes.env.vars.RABBITMQ_PORT=5672
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to deploy $service.${NC}"
        exit 1
    fi

    echo -e "${YELLOW}Setting runtime environment for $service...${NC}"
    oc set env deployment/$service \
        QUARKUS_DATASOURCE_USERNAME=trademesh \
        QUARKUS_DATASOURCE_PASSWORD=trademesh-secret \
        MP_MESSAGING_CONNECTOR_SMALLRYE_RABBITMQ_USERNAME=trademesh \
        MP_MESSAGING_CONNECTOR_SMALLRYE_RABBITMQ_PASSWORD=trademesh-secret \
        RABBITMQ_PORT=5672
done

echo -e "${GREEN}=== DEPLOYMENT COMPLETE ===${NC}"
oc get pods
