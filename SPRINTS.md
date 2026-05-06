# E-Commerce Backend Project Plan – Phases & Sprints

## Phase 1: Foundation

### Sprint 1: Project Bootstrap ✅ COMPLETE
- [x] Spring Initializr setup (Web, Data JPA, Kafka, Actuator, PostgreSQL, Elasticsearch)
- [x] Create base directory structure per service
- [x] Configure PostgreSQL, Kafka, Redis, Elasticsearch in docker-compose
- [x] Setup Flyway/Liquibase for migrations

### Sprint 2: Local Development Environment ✅ COMPLETE
- [x] Docker Compose configuration (postgres, kafka, redis, elasticsearch, kibana, mailpit)
- [x] Spring Boot DevTools setup
- [x] Hot reload configuration (JRebel/Spring Loaded)
- [x] API mocking infrastructure (WireMock + mock controllers)
- [x] Developer scripts (dev-up.sh, mock-server.sh)
- [x] Remote debugging configuration

---

## Phase 2: Core Services

### Sprint 3: Product Service ✅ COMPLETE
- [x] Domain entities (products, categories, inventory)
- [x] REST API controllers (CRUD operations)
- [x] Elasticsearch integration for catalog search
- [x] Inventory read-model setup
- [ ] Unit tests (80%+ coverage target) *- deferred to Phase 6*
- [x] API documentation (SpringDoc/OpenAPI)

### Sprint 4: Order Service ⬜ INCOMPLETE
- [ ] Domain entities (orders, order_items, carts)
- [ ] Cart management (add, update, remove items)
- [ ] Checkout flow implementation
- [ ] Order lifecycle management
- [ ] JSONB cart storage

### Sprint 5: Payment Service ⬜ INCOMPLETE
- [ ] Payment domain entity
- [ ] PSP integration (Stripe/PayPal simulation)
- [ ] Payment status management
- [ ] Transaction tracking
- [ ] Idempotency key handling

---

## Phase 3: Event-Driven Architecture & Authentication

### Sprint 6: Kafka & Saga Pattern Implementation ⬜ INCOMPLETE
- [ ] Kafka topics setup (order.created, order.cancelled, payment.completed, payment.failed, inventory.reserved, inventory.failed, order.shipped)
- [ ] Product Service → emit product.created events
- [ ] Order Service → emit order.created (PENDING)
- [ ] Inventory Service → consume, reserve stock, emit inventory.reserved
- [ ] Payment Service → consume, process payment, emit payment.completed/failed
- [ ] Order Service → update status to CONFIRMED on success
- [ ] Compensating transactions (restore stock on payment failure)
- [ ] Consider Temporal/Camunda for complex orchestration

### Sprint 7: User Service ⬜ INCOMPLETE
- [ ] User domain entity (users, addresses)
- [ ] JWT authentication implementation
- [ ] User registration & profiles
- [ ] Address management
- [ ] Redis JWT denylist/blocklist
- [ ] OAuth2 setup

### Sprint 8: Notification Service ⬜ INCOMPLETE
- [ ] Kafka consumer for order events
- [ ] Email notifications (order confirmations, updates)
- [ ] Integration with mailpit for local dev

---

## Phase 4: API Gateway & Infrastructure

### Sprint 9: API Gateway & Service Mesh ⬜ INCOMPLETE
- [ ] Kong/Traefik/Spring Cloud Gateway setup
- [ ] Rate limiting & routing
- [ ] Istio/Linkerd service mesh setup
- [ ] mTLS configuration

### Sprint 10: Observability & Error Handling ⬜ INCOMPLETE
- [ ] OpenTelemetry (OTLP) integration
- [ ] Prometheus metrics setup
- [ ] Grafana dashboards (health, throughput, latency, error rates)
- [ ] Loki for structured logging
- [ ] Jaeger/Tempo distributed tracing
- [ ] SLO/SLI definitions
- [ ] Alertmanager (P1-P4 severity routing)
- [ ] RFC-7807 ProblemDetail error handling
- [ ] Idempotency key filters

---

## Phase 5: Kubernetes & Production Deployment

### Sprint 11: Kubernetes Deployment ⬜ INCOMPLETE
- [ ] Kind/Minikube local cluster setup
- [ ] Dockerfiles for all services
- [ ] K8s deployments, services, configs
- [ ] Config Maps & Secrets management
- [ ] Service discovery (K8s CoreDNS, no Eureka)
- [ ] Spring Cloud Config integration

### Sprint 12: Production Hardening ⬜ INCOMPLETE
- [ ] GraalVM native image builds (optional)
- [ ] Database read replica configuration
- [ ] Redis session caching
- [ ] CDN/WAF configuration
- [ ] EKS/GKE deployment preparation

---

## Phase 6: Testing & Quality Assurance

### Sprint 13: Testing Strategy Implementation ⬜ INCOMPLETE
- [ ] Unit tests with JUnit 5 + Mockito (80%+ coverage)
- [ ] Integration tests with Testcontainers
- [ ] API contract tests (Pact)
- [ ] Spring Boot Test slices (@DataJpaTest, @WebMvcTest)
- [ ] Embedded Kafka tests
- [ ] End-to-end tests (Cypress/Playwright)
- [ ] RestAssured API flow tests

### Sprint 14: Performance & Security ⬜ INCOMPLETE
- [ ] Gatling load testing (Black Friday scenarios)
- [ ] JMeter stress & soak tests
- [ ] Chaos testing (service failures, network partitions)
- [ ] OWASP ZAP security scanning
- [ ] Snyk/Trivy dependency CVE detection
- [ ] SonarQube quality gates (80% coverage, 0 critical bugs)
- [ ] Performance budget (p95 latency < 200ms)
- [ ] Quarterly penetration testing schedule

---

## Summary

| Phase | Name | Sprints | Status |
|-------|------|---------|--------|
| 1 | Foundation | 1-2 | ✅ Done |
| 2 | Core Services | 3-5 | ⬜ To Do |
| 3 | Event-Driven & Auth | 6-8 | ⬜ To Do |
| 4 | API Gateway & Infrastructure | 9-10 | ⬜ To Do |
| 5 | Kubernetes & Deployment | 11-12 | ⬜ To Do |
| 6 | Testing & Quality | 13-14 | ⬜ To Do |

**Total: 14 Sprints across 6 Phases**

---

## Current Progress Tracking

Update below as sprints are completed:

### Phase 1: Foundation
- Sprint 1: ✅
- Sprint 2: ✅

### Sprint 2 Enhancements (post-review):
- [x] Fixed Dockerfile.dev COPY logic (build context is per-service, paths were wrong)
- [x] Removed unused ARG SERVICE_NAME from all Dockerfile.dev files
- [x] Made all dev scripts executable
- [x] Enhanced dev-up.sh with --build flag and targeted service startup
- [x] Added dev-logs.sh script for tailing container logs
- [x] Added dev-reset.sh script for full environment reset (with confirmation prompt)
- [x] Fixed swapped port mappings for inventory-service (8086) and notification-service (8085)
- [x] Added application-mock.yml profile for product-service
- [x] Added MockProductController (mock endpoints for frontend dev)
- [x] Expanded WireMock stubs: product-by-id, orders-list, orders-create, orders-by-id, payments

### Phase 2: Core Services
- Sprint 3: ✅
- Sprint 4: ✅
- Sprint 5: ⬜

### Phase 3: Event-Driven & Auth
- Sprint 6: ⬜
- Sprint 7: ⬜
- Sprint 8: ⬜

### Phase 4: API Gateway & Infrastructure
- Sprint 9: ⬜
- Sprint 10: ⬜

### Phase 5: Kubernetes & Deployment
- Sprint 11: ⬜
- Sprint 12: ⬜

### Phase 6: Testing & Quality
- Sprint 13: ⬜
- Sprint 14: ⬜