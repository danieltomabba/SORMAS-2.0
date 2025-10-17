#!/bin/bash

# SORMAS AI Services Startup Script
# This script starts all AI-related services for SORMAS

set -e

echo "==================================="
echo "SORMAS AI Services Startup"
echo "==================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}Warning: docker-compose not found, using 'docker compose' instead${NC}"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Function to check service health
check_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}Waiting for $service_name to be ready...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if curl -f $url > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name is ready!${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo -e "${RED}✗ $service_name failed to start${NC}"
    return 1
}

# Start services
echo "Starting Docker services..."
$DOCKER_COMPOSE up -d

echo ""
echo "Services starting..."
echo ""

# Wait for services to be ready
check_service "PostgreSQL" "localhost:5432" || true
check_service "AI Service" "http://localhost:8000/health"
check_service "MLflow" "http://localhost:5000"

echo ""
echo "==================================="
echo -e "${GREEN}All services are running!${NC}"
echo "==================================="
echo ""
echo "Service URLs:"
echo "  - AI Service API: http://localhost:8000"
echo "  - AI Service Docs: http://localhost:8000/docs"
echo "  - MLflow UI: http://localhost:5000"
echo "  - PostgreSQL: localhost:5432"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f sormas-ai"
echo ""
echo "To stop services:"
echo "  docker-compose down"
echo ""
echo "To view AI dashboard (after SORMAS starts):"
echo "  http://localhost:6080/sormas-flow/ai/outbreak-dashboard"
echo ""
echo "==================================="
