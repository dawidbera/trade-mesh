#!/bin/bash

# --- TradeMesh Launcher ---
# Autonomous script to run the entire TradeMesh technology stack.

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== TradeMesh System Launcher ===${NC}"

# 1. Dependency Check
check_dep() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}Error: $1 is not installed.${NC}"
        exit 1
    fi
}

check_dep "java"
check_dep "node"
check_dep "docker"

# Check if Docker is running
if ! docker ps &> /dev/null; then
    echo -e "${RED}Error: Docker is not running. Please start Docker/Podman engine.${NC}"
    exit 1
fi

# 1.5 Cleanup old processes
echo -e "${YELLOW}Cleaning up ports and processes...${NC}"
pkill -f 'java' 2>/dev/null
pkill -f 'quarkus' 2>/dev/null
# Kill processes on specific ports (gRPC and HTTP)
for port in 8080 8081 8082 8083 8084 9000 9001 9002 9003 4200; do
    fuser -k ${port}/tcp 2>/dev/null
done
sleep 2

# 2. Build function (optional)
BUILD=false
if [[ "$1" == "--build" ]]; then
    BUILD=true
fi

SERVICES=(
    "market-data-service"
    "analytics-service"
    "history-service"
    "gateway-service"
)

if [ "$BUILD" = true ]; then
    echo -e "${GREEN}Rebuilding all services (Maven)...${NC}"
    for service in "${SERVICES[@]}"; do
        echo -e "${BLUE}Building $service...${NC}"
        cd "$service" || exit
        ./mvnw clean install -DskipTests -q
        cd ..
    done
    
    echo -e "${GREEN}Building Frontend (npm)...${NC}"
    cd frontend || exit
    npm run build -- --configuration development
    cd ..
fi

# 3. Cleanup function on exit
PIDS=()
cleanup() {
    echo -e "
${RED}Closing all services...${NC}"
    for pid in "${PIDS[@]}"; do
        kill -15 "$pid" 2>/dev/null
    done
    echo -e "${YELLOW}Stopping RabbitMQ and Keycloak...${NC}"
    docker stop rabbit-trade 2>/dev/null
    docker stop keycloak-trade 2>/dev/null
    exit
}

trap cleanup SIGINT SIGTERM

# 3.5 Start RabbitMQ and Keycloak
echo -e "${GREEN}Starting RabbitMQ (Docker)...${NC}"
docker rm -f rabbit-trade 2>/dev/null
docker run -d --name rabbit-trade -p 5672:5672 -p 15672:15672 rabbitmq:3-management

echo -e "${GREEN}Starting Keycloak (Docker)...${NC}"
docker rm -f keycloak-trade 2>/dev/null
docker run -d --name keycloak-trade -p 8180:8080 \
    -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
    quay.io/keycloak/keycloak:26.0.0 start-dev --import-realm

# 4. Start Backends (Quarkus)
echo -e "${GREEN}Starting backend services (Quarkus Dev Mode)...${NC}"

# Port allocation (unique for each service)
for i in "${!SERVICES[@]}"; do
    service="${SERVICES[$i]}"
    echo -e "${BLUE}Starting $service...${NC}"
    cd "$service" || exit
    
    # Ports: 8081, 8082, 8083, 8084
    PORT=$((8081 + i))
    
    ./mvnw quarkus:dev -Dquarkus.http.port=$PORT -Ddebug=false > "../logs-$service.log" 2>&1 &
    PIDS+=($!)
    cd ..
    sleep 2 # Pause for initialization
done

# 5. Start Frontend (Angular)
echo -e "${GREEN}Starting Frontend (Angular)...${NC}"
cd frontend || exit
npm start > "../logs-frontend.log" 2>&1 &
PIDS+=($!)
cd ..

echo -e "${GREEN}=== SYSTEM STARTED ===${NC}"
echo -e "Dashboard: ${BLUE}http://localhost:4200${NC}"
echo -e "GraphQL UI: ${BLUE}http://localhost:8084/q/graphql-ui${NC}"
echo -e "Service logs can be found in: logs-*.log"
echo -e "${BLUE}Press [Ctrl+C] to stop all services.${NC}"

# Wait for background processes
wait
