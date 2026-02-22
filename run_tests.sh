#!/bin/bash

# --- TradeMesh Test Runner (Fail-Fast) ---
# Script to run tests with error reporting and statistics.

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=== TradeMesh Test Suite (Stop-on-Failure) ===${NC}"

SERVICES=(
    "market-data-service"
    "analytics-service"
    "history-service"
    "gateway-service"
)

TOTAL_RUN=0
TOTAL_FAIL=0
TOTAL_ERR=0

# Function to calculate percentages
calc_percent() {
    if [ $1 -eq 0 ]; then echo "0"; else
        echo "scale=2; (($1 - $2 - $3) / $1) * 100" | bc
    fi
}

# 1. Backend tests (Java/JUnit)
for service in "${SERVICES[@]}"; do
    echo -e "${GREEN}Testing $service...${NC}"
    cd "$service" || exit
    
    # Run tests, save results to file, stop on 1st error
    # -q for less noise, but we need to extract results
    ./mvnw test -Dsurefire.skipAfterFailureCount=1 > test_results.log 2>&1
    RET=$?
    
    # Extract statistics: "Tests run: 5, Failures: 1, Errors: 0, Skipped: 0"
    STATS=$(grep "Tests run:" test_results.log | tail -n 1)
    RUN=$(echo "$STATS" | sed -n 's/.*Tests run: \([0-9]*\).*/\1/p')
    FAIL=$(echo "$STATS" | sed -n 's/.*Failures: \([0-9]*\).*/\1/p')
    ERR=$(echo "$STATS" | sed -n 's/.*Errors: \([0-9]*\).*/\1/p')
    
    # If sed didn't find anything (e.g. compilation error), set defaults
    RUN=${RUN:-0}
    FAIL=${FAIL:-0}
    ERR=${ERR:-0}
    
    TOTAL_RUN=$((TOTAL_RUN + RUN))
    TOTAL_FAIL=$((TOTAL_FAIL + FAIL))
    TOTAL_ERR=$((TOTAL_ERR + ERR))
    
    if [ $RET -ne 0 ]; then
        echo -e "${RED}FAILURE in $service!${NC}"
        
        # Try to extract the name of the method that failed
        FAILED_METHOD=$(grep -E "<<< FAILURE!|<<< ERROR!" test_results.log | head -n 1 | sed 's/ <<<.*//' | sed 's/.*in //')
        if [ -z "$FAILED_METHOD" ]; then
            # Alternative surefire format
            FAILED_METHOD=$(grep -A 1 "Failures:" test_results.log | grep "  " | head -n 1)
        fi
        
        PERCENT=$(calc_percent $TOTAL_RUN $TOTAL_FAIL $TOTAL_ERR)
        echo -e "${YELLOW}Failed Method: ${RED}${FAILED_METHOD}${NC}"
        echo -e "${YELLOW}Progress before failure: ${PERCENT}% success of ${TOTAL_RUN} executed tests.${NC}"
        
        rm test_results.log
        exit 1
    fi
    
    rm test_results.log
    cd ..
done

# 2. Frontend tests (Angular)
echo -e "${GREEN}Testing frontend (Angular)...${NC}"
cd frontend || exit
# Angular/Karma: stop-on-first-failure is harder from CLI without karma.conf changes,
# but we'll check the overall result.
npm run test -- --watch=false
RET=$?

if [ $RET -ne 0 ]; then
    echo -e "${RED}FAILURE in frontend!${NC}"
    PERCENT=$(calc_percent $TOTAL_RUN $TOTAL_FAIL $TOTAL_ERR)
    echo -e "${YELLOW}Reason: Frontend tests failed. Please review the output above for details.${NC}"
    echo -e "${YELLOW}Final Score: ${PERCENT}% success based on backend tests.${NC}"
    exit 1
fi
cd ..

echo -e "${BLUE}=== ALL TESTS PASSED (100% Success) ===${NC}"
echo -e "${GREEN}Total tests executed: $TOTAL_RUN${NC}"
