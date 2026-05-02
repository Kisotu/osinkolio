#!/bin/bash
# dev-logs.sh - Tail logs from development services
# Usage: ./scripts/dev-logs.sh [service_name]
#   Without arguments, tails all services.
#   With a service name, tails only that service.
#   Examples: ./scripts/dev-logs.sh product-service
#             ./scripts/dev-logs.sh postgres

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILES="-f ${PROJECT_DIR}/docker-compose.yml -f ${PROJECT_DIR}/docker-compose.dev.yml"

cd "$PROJECT_DIR"

if [ $# -eq 0 ]; then
  echo "Tailing logs for all services..."
  docker compose $COMPOSE_FILES logs -f
else
  echo "Tailing logs for: $*"
  docker compose $COMPOSE_FILES logs -f "$@"
fi