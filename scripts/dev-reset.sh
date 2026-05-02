#!/bin/bash
# dev-reset.sh - Reset full development environment
# Stops all services, removes volumes (data loss!), and restarts infrastructure.
# Usage: ./scripts/dev-reset.sh [--build]
#   --build   Rebuild Docker images after reset

set -euo pipefail

BUILD_FLAG=""
if [ "${1:-}" = "--build" ]; then
  BUILD_FLAG="--build"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILES="-f ${PROJECT_DIR}/docker-compose.yml -f ${PROJECT_DIR}/docker-compose.dev.yml"

cd "$PROJECT_DIR"

echo "========================================="
echo "WARNING: This will delete all data volumes!"
echo "  - PostgreSQL databases"
echo "  - Kafka topics and data"
echo "  - Redis cache"
echo "  - Elasticsearch indices"
echo "========================================="
echo ""
read -rp "Are you sure? (type 'yes' to confirm): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
  echo "Reset cancelled."
  exit 1
fi

echo ""
echo "Stopping all services and removing volumes..."
docker compose $COMPOSE_FILES down -v

echo ""
echo "Starting infrastructure services..."
docker compose $COMPOSE_FILES up -d $BUILD_FLAG postgres kafka redis elasticsearch kibana mailpit

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until docker compose $COMPOSE_FILES exec -T postgres pg_isready -U admin -d musicstore > /dev/null 2>&1; do
  sleep 2
done
echo "PostgreSQL is ready!"

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
until docker compose $COMPOSE_FILES exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
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
echo "Development environment has been reset!"
echo "========================================="
echo ""
echo "All data volumes have been wiped and recreated."
echo "Run ./scripts/dev-up.sh <service> to start application services."