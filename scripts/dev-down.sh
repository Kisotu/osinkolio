#!/bin/bash
# dev-down.sh - Stop development environment

set -e

echo "Stopping Music Store development environment..."
docker compose down

echo "Development environment stopped!"