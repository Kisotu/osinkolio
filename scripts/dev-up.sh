#!/bin/bash
# dev-up.sh - Start full development environment
# Usage: ./scripts/dev-up.sh [--build] [service1 service2 ...]
#   --build       Rebuild Docker images before starting
#   service(s)    Start only specified services (e.g. product-service order-service)

set -euo pipefail

BUILD_FLAG=""
SERVICES=()

for arg in "$@"; do
  if [ "$arg" = "--build" ]; then
    BUILD_FLAG="--build"
  else
    SERVICES+=("$arg")
  fi
done

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILES="-f ${PROJECT_DIR}/docker-compose.yml -f ${PROJECT_DIR}/docker-compose.dev.yml"

cd "$PROJECT_DIR"

echo "Starting Music Store development environment..."

if [ ${#SERVICES[@]} -eq 0 ]; then
  INFRA_SERVICES="postgres kafka redis elasticsearch kibana mailpit"
  ALL_SERVICES="$INFRA_SERVICES product-service order-service payment-service user-service inventory-service notification-service api-gateway"
  if [ -n "$BUILD_FLAG" ]; then
    docker compose $COMPOSE_FILES up -d $BUILD_FLAG $ALL_SERVICES
  else
    docker compose $COMPOSE_FILES up -d $INFRA_SERVICES
    echo ""
    echo "Infrastructure services started. Run individual services via your IDE or:"
    echo "  ./scripts/dev-up.sh product-service"
    echo "  ./scripts/dev-up.sh order-service"
    echo "  etc."
  fi
else
  docker compose $COMPOSE_FILES up -d $BUILD_FLAG "${SERVICES[@]}"
fi

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker compose exec -T postgres pg_isready -U admin -d musicstore > /dev/null 2>&1; do
  sleep 2
done

echo "PostgreSQL is ready!"

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
until docker compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
  sleep 2
done

echo "Kafka is ready!"

# Wait for Elasticsearch to be ready
echo "Waiting for Elasticsearch to be ready..."
until curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green"\|"status":"yellow"'; do
  sleep 2
done

echo "Elasticsearch is ready!"

echo ""
echo "========================================="
echo "Development environment is ready!"
echo "========================================="
echo ""
echo "Infrastructure Services:"
echo "  PostgreSQL:    localhost:5432"
echo "  Kafka:         localhost:9092"
echo "  Redis:         localhost:6379"
echo "  Elasticsearch: localhost:9200"
echo "  Kibana:        localhost:5601"
echo "  Mailpit UI:    localhost:8025"
echo ""
echo "Application Services (when running):"
echo "  Product Service:     localhost:8081"
echo "  Order Service:       localhost:8082"
echo "  Payment Service:     localhost:8083"
echo "  User Service:        localhost:8084"
echo "  Inventory Service:   localhost:8085"
echo "  Notification Service: localhost:8086"
echo "  API Gateway:         localhost:8087"
echo ""
echo "Debug Ports: 5005-5011 (one per service)"
echo ""
echo "To stop:  ./scripts/dev-down.sh"
echo "To logs:  ./scripts/dev-logs.sh [service]"
echo "To reset: ./scripts/dev-reset.sh"