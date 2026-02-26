#!/bin/bash

# --- TradeMesh Frontend OpenShift Deployer ---
# Purpose: Build Angular frontend and deploy to OpenShift Sandbox via Binary Build.

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FRONTEND_DIR="$SCRIPT_DIR/frontend"

echo -e "${BLUE}=== TradeMesh Frontend Deployer ===${NC}"

# 1. Build locally
echo -e "${YELLOW}Building Angular application locally...${NC}"
cd "$FRONTEND_DIR" || exit
npm install --silent
npm run build -- --configuration production

if [ ! -d "dist/frontend/browser" ]; then
    echo -e "${RED}Error: Build failed. Directory dist/frontend/browser not found.${NC}"
    exit 1
fi

# 2. Prepare OpenShift Resources
NAMESPACE=$(oc project -q)
echo -e "${GREEN}Deploying to namespace: ${BLUE}$NAMESPACE${NC}"

# Create ImageStream if it doesn't exist
oc create imagestream frontend --ignore-not-found

# Create Binary BuildConfig
echo -e "${YELLOW}Creating/Updating BuildConfig...${NC}"
oc get bc frontend &> /dev/null
if [ $? -ne 0 ]; then
    # Use Nginx S2I image from OpenShift catalog
    oc new-build --name=frontend --image-stream=openshift/nginx:latest --binary=true
else
    echo -e "BuildConfig 'frontend' already exists."
fi

# 3. Start Build
echo -e "${YELLOW}Uploading build artifacts to OpenShift...${NC}"
# We only need the files from dist/frontend/browser for nginx
oc start-build frontend --from-dir=dist/frontend/browser --follow

# 4. Create Deployment and Route if they don't exist
echo -e "${YELLOW}Ensuring Deployment and Route exist...${NC}"
oc get dc frontend &> /dev/null
if [ $? -ne 0 ]; then
    oc new-app frontend
    oc expose svc/frontend
else
    echo -e "Deployment 'frontend' already exists. Triggering redeploy..."
    oc rollout latest dc/frontend
fi

echo -e "${GREEN}=== FRONTEND DEPLOYMENT COMPLETE ===${NC}"
echo -e "Public URL: ${BLUE}$(oc get route frontend -o jsonpath='{.spec.host}')${NC}"
