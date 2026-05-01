#!/bin/bash
# dev-up.sh - Start full development environment

set -e

echo "Starting Music Store development environment..."

# Start base infrastructure
docker compose up -d postgres kafka redis elasticsearch kibana mailpit

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
echo "Services:"
echo "  PostgreSQL:    localhost:5432"
echo "  Kafka:         localhost:9092"
echo "  Redis:         localhost:6379"
echo "  Elasticsearch: localhost:9200"
echo "  Kibana:        localhost:5601"
echo "  Mailpit UI:    localhost:8025"
echo ""
echo "To stop: docker compose down"
echo "To view logs: docker compose logs -f"