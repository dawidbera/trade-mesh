#!/bin/bash

# --- TradeMesh OpenShift Deployment Automator ---
# Purpose: Fully deploy the TradeMesh stack to Red Hat OpenShift Sandbox.

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=== TradeMesh OpenShift Deployer ===${NC}"

# 1. Connectivity Check
echo -e "${YELLOW}Checking OpenShift connectivity...${NC}"
if ! oc whoami &> /dev/null; then
    echo -e "${RED}Error: Not logged into OpenShift. Please run 'oc login' first.${NC}"
    exit 1
fi

NAMESPACE=$(oc project -q)
echo -e "${GREEN}Deploying to namespace: ${BLUE}$NAMESPACE${NC}"

# 2. Infrastructure Layer (Terraform)
echo -e "${YELLOW}Deploying Infrastructure (Postgres, Redis, RabbitMQ)...${NC}"
cd trade-mesh/infra/terraform || exit
terraform init -reconfigure
terraform apply -auto-approve -var="namespace=$NAMESPACE"
if [ $? -ne 0 ]; then
    echo -e "${RED}Terraform failed. Check your Sandbox resource limits.${NC}"
    exit 1
fi
cd ../../..

# 3. Backend Services (Quarkus)
SERVICES=(
    "market-data-service"
    "analytics-service"
    "history-service"
    "gateway-service"
)

echo -e "${YELLOW}Building and Deploying Backend Services...${NC}"
for service in "${SERVICES[@]}"; do
    echo -e "${BLUE}Deploying $service...${NC}"
    cd "trade-mesh/$service" || exit
    
    # -Dquarkus.kubernetes.deploy=true triggers the actual OCP build/deploy
    ./mvnw clean package 
        -DskipTests 
        -Dquarkus.container-image.group="$NAMESPACE" 
        -Dquarkus.kubernetes.deploy=true 
        -Dquarkus.openshift.route.expose=true
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to deploy $service.${NC}"
        exit 1
    fi
    cd ../..
done

# 4. Frontend (Angular)
# Note: In a real OCP environment, we typically use an Nginx-based image for the frontend.
# This part assumes we are using a binary build or similar strategy.
echo -e "${YELLOW}Frontend deployment: Please use the provided OpenShift BuildConfig for Angular.${NC}"
echo -e "${BLUE}Note: Ensure your Gateway Route URL is updated in environments/environment.prod.ts${NC}"

echo -e "${GREEN}=== DEPLOYMENT INITIATED ===${NC}"
echo -e "Monitor progress: ${BLUE}oc get pods -w${NC}"
echo -e "Once pods are RUNNING, check your Routes: ${BLUE}oc get routes${NC}"
