# 📚 Documentation Hub

Welcome to the E-commerce Backend documentation! This page serves as your central navigation point for all technical documentation.

---

## 🚀 Quick Links

| I want to... | Go to |
|--------------|-------|
| **Get started as a new developer** | [ONBOARDING.md](ONBOARDING.md) |
| **Set up my local environment** | [DEVELOPMENT.md](DEVELOPMENT.md) |
| **Understand the system architecture** | [architecture/system-overview.md](architecture/system-overview.md) |
| **View database schemas** | [architecture/data-model.md](architecture/data-model.md) |
| **Find API endpoints** | [Swagger UI](#api-documentation) |
| **Debug a production issue** | [TROUBLESHOOTING.md](TROUBLESHOOTING.md) *(Coming Soon)* |
| **Handle an alert** | [runbooks/](runbooks/) *(Coming Soon)* |
| **Deploy to production** | [DEPLOYMENT.md](DEPLOYMENT.md) *(Coming Soon)* |
| **Understand a design decision** | [adr/](adr/) *(Coming Soon)* |

---

## 📖 Documentation Categories

### 🎓 Getting Started

Perfect for new team members:

- **[Onboarding Guide](ONBOARDING.md)** - 3-day ramp-up plan for new developers
- **[Development Guide](DEVELOPMENT.md)** - Local environment setup and common tasks
- **[Glossary](GLOSSARY.md)** *(Coming Soon)* - Domain terminology dictionary

### 🏗️ Architecture

Understand the system design:

- **[System Overview](architecture/system-overview.md)** - High-level architecture and technology stack
- **[Data Model](architecture/data-model.md)** - Database schemas and entity relationships
- **[Order Saga Flow](architecture/order-saga-flow.md)** - Distributed transaction orchestration
- **[Outbox Pattern](architecture/outbox-pattern.md)** - Reliable event delivery
- **[Event Catalog](architecture/event-catalog.md)** *(Coming Soon)* - All Kafka events reference

### 🔌 API Documentation

REST API contracts and conventions:

#### Swagger UI (Interactive)

Each service exposes interactive API documentation:

| Service | Swagger UI | Port |
|---------|------------|------|
| **Product Service** | http://localhost:8081/swagger-ui.html | 8081 |
| **Inventory Service** | http://localhost:8082/swagger-ui.html | 8082 |
| **Order Service** | http://localhost:8083/swagger-ui.html | 8083 |
| **Payment Service** | http://localhost:8084/swagger-ui.html *(Coming Soon)* | 8084 |
| **Notification Service** | http://localhost:8085/swagger-ui.html *(Coming Soon)* | 8085 |
| **Auth Service** | http://localhost:8086/swagger-ui.html *(Coming Soon)* | 8086 |

#### API Reference

- **[API Conventions & Best Practices](api/README.md)** - REST API design guidelines, authentication, error handling
- **[OpenAPI Specs](api/)** - Download OpenAPI 3.0 specifications

### 🛠️ Operations

Production deployment and operations:

- **[Deployment Guide](DEPLOYMENT.md)** *(Coming Soon)* - CI/CD pipelines and deployment procedures
- **[Configuration Reference](CONFIGURATION.md)** *(Coming Soon)* - Environment variables and feature flags
- **[Troubleshooting Guide](TROUBLESHOOTING.md)** *(Coming Soon)* - Common issues and solutions
- **[Security](SECURITY.md)** - Security architecture and best practices
- **[Observability](OBSERVABILITY.md)** - Monitoring, tracing, and logging
- **[Performance](PERFORMANCE.md)** *(Coming Soon)* - SLA targets and benchmarks

### 📋 Runbooks

Incident response procedures:

- **[Runbook Index](runbooks/README.md)** *(Coming Soon)*
- **[Kafka Consumer Lag](runbooks/kafka-consumer-lag.md)** *(Coming Soon)*
- **[Database Connection Pool](runbooks/database-connection-pool.md)** *(Coming Soon)*
- **[High Memory Usage](runbooks/high-memory-usage.md)** *(Coming Soon)*
- **[Order Stuck Pending](runbooks/order-stuck-pending.md)** *(Coming Soon)*
- **[Service Restart](runbooks/service-restart.md)** *(Coming Soon)*
- **[Disaster Recovery](runbooks/disaster-recovery.md)** *(Coming Soon)*

### 🧪 Testing

Quality assurance and testing:

- **[Testing Guide](TESTING.md)** *(Coming Soon)* - Testing strategy and guidelines
- **[Integration Tests](../*/src/test/)** - Service-specific test suites

### 📝 Architecture Decision Records (ADR)

Historical context for design decisions:

- **[ADR Index](adr/README.md)** *(Coming Soon)*
- **[ADR-001: Hexagonal Architecture](adr/001-hexagonal-architecture.md)** *(Coming Soon)*
- **[ADR-002: Kafka over RabbitMQ](adr/002-kafka-over-rabbitmq.md)** *(Coming Soon)*
- **[ADR-003: Saga Orchestration Pattern](adr/003-saga-orchestration-pattern.md)** *(Coming Soon)*
- **[ADR-004: PostgreSQL per Service](adr/004-postgresql-per-service.md)** *(Coming Soon)*
- **[ADR-005: ULID over UUID](adr/005-ulid-over-uuid.md)** *(Coming Soon)*

### 🔧 Service Catalog

Per-service detailed documentation:

- **[Service Overview](services/README.md)** *(Coming Soon)*
- **[API Gateway](services/api-gateway.md)** *(Coming Soon)*
- **[Auth Service](services/auth-service.md)** *(Coming Soon)*
- **[Product Service](services/product-service.md)** *(Coming Soon)*
- **[Inventory Service](services/inventory-service.md)** *(Coming Soon)*
- **[Order Service](services/order-service.md)** *(Coming Soon)*
- **[Payment Service](services/payment-service.md)** *(Coming Soon)*
- **[Notification Service](services/notification-service.md)** *(Coming Soon)*

---

## 🗺️ Documentation Roadmap

### ✅ Completed (Phase 1)

- [x] Onboarding Guide
- [x] Development Guide
- [x] System Architecture Overview
- [x] Data Model Documentation
- [x] Order Saga Flow
- [x] Outbox Pattern
- [x] Security Overview
- [x] Observability Setup

### 🚧 In Progress (Phase 2)

- [ ] API Documentation (OpenAPI specs)
- [ ] Configuration Reference
- [ ] Troubleshooting Guide
- [ ] Operations Runbooks
- [ ] Event Catalog

### 📅 Planned (Phase 3)

- [ ] Architecture Decision Records
- [ ] Glossary
- [ ] Testing Guide
- [ ] Performance Guide
- [ ] Service Catalog

---

## 🔍 Finding Information

### By Role

**New Developer**:
1. Start with [ONBOARDING.md](ONBOARDING.md)
2. Follow [DEVELOPMENT.md](DEVELOPMENT.md) to set up your environment
3. Read [architecture/system-overview.md](architecture/system-overview.md) to understand the system

**Backend Developer**:
1. Check [Swagger UI](#api-documentation) for API contracts
2. Review [architecture/data-model.md](architecture/data-model.md) for database schemas
3. Understand patterns: [Saga](architecture/order-saga-flow.md), [Outbox](architecture/outbox-pattern.md)

**DevOps Engineer**:
1. Read [DEPLOYMENT.md](DEPLOYMENT.md) *(Coming Soon)*
2. Review [OBSERVABILITY.md](OBSERVABILITY.md)
3. Familiarize with [runbooks/](runbooks/) *(Coming Soon)*

**Architect**:
1. Review [adr/](adr/) *(Coming Soon)* for design decisions
2. Check [architecture/](architecture/) for system design
3. Review [PERFORMANCE.md](PERFORMANCE.md) *(Coming Soon)* for SLA targets

### By Topic

**Authentication & Authorization**:
- [SECURITY.md](SECURITY.md)
- [Auth Service Swagger](http://localhost:8086/swagger-ui.html) *(Coming Soon)*

**Order Processing**:
- [Order Saga Flow](architecture/order-saga-flow.md)
- [Order Service Swagger](http://localhost:8083/swagger-ui.html)

**Event-Driven Architecture**:
- [Outbox Pattern](architecture/outbox-pattern.md)
- [Event Catalog](architecture/event-catalog.md) *(Coming Soon)*

**Database Design**:
- [Data Model](architecture/data-model.md)
- [Migration Guide](DEVELOPMENT.md#database-migrations)

---

## 🛠️ Tools & Dashboards

### Development Tools

| Tool | URL | Purpose |
|------|-----|---------|
| **Swagger UI** | http://localhost:808X/swagger-ui.html | API documentation (X = service port) |
| **Kafka UI** | http://localhost:8090 | Kafka topic management |
| **Zipkin** | http://localhost:9411 | Distributed tracing |
| **Grafana** | http://localhost:3000 | Monitoring dashboards (admin/admin) |
| **Prometheus** | http://localhost:9090 | Metrics collection |

### Database Access

```bash
# Connect to service databases
psql -h localhost -U postgres -d product_db
psql -h localhost -U postgres -d inventory_db
psql -h localhost -U postgres -d order_db
psql -h localhost -U postgres -d payment_db
psql -h localhost -U postgres -d auth_db
```

Default credentials: `postgres` / `postgres`

---

## 📞 Getting Help

### Internal Resources

- **Slack Channels**:
  - `#ecommerce-backend` - General discussions
  - `#ecommerce-alerts` - Production alerts
  - `#ecommerce-deployments` - Deployment notifications

- **Key Contacts**:
  - Tech Lead: @tech-lead
  - DevOps: @devops-team
  - Solution Architect: @solution-architect

### External Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Microservices Patterns](https://microservices.io/patterns/)

---

## 📝 Contributing to Documentation

### Documentation Standards

- **Format**: GitHub-flavored Markdown
- **Diagrams**: Use Mermaid for consistency
- **Code Examples**: Always tested and working
- **Links**: Use relative paths for internal docs

### Updating Documentation

1. Create a branch: `git checkout -b docs/update-xyz`
2. Make your changes
3. Verify all links work
4. Create a Pull Request
5. Tag `@tech-lead` for review

### Documentation Review Checklist

- [ ] Markdown lints without errors
- [ ] All links are valid
- [ ] Mermaid diagrams render correctly
- [ ] Code examples are tested
- [ ] No sensitive information exposed

---

## 📊 Documentation Metrics

| Metric | Target | Current |
|--------|--------|---------|
| **Documentation Coverage** | 90% | ~40% |
| **Freshness** (< 30 days old) | 100% | ~60% |
| **Developer Onboarding Time** | 3 days | ~2 weeks |
| **Time to Find API Contract** | < 1 min | Manual search |

**Last Updated**: 2026-01-19  
**Next Review**: 2026-02-19

---

## 🎯 Quick Reference Card

```
┌─────────────────────────────────────────────────────────────┐
│  E-COMMERCE BACKEND - QUICK REFERENCE                       │
├─────────────────────────────────────────────────────────────┤
│  🚀 Start Infrastructure:  cd docker && docker-compose up -d│
│  🔨 Build All Services:    ./gradlew build                  │
│  ▶️  Run Service:           ./gradlew :service-name:bootRun │
│  🧪 Run Tests:             ./gradlew test                   │
│  📊 View Logs:             docker-compose logs -f           │
│  🔍 Check Health:          curl localhost:808X/actuator/health│
│  📚 API Docs:              localhost:808X/swagger-ui.html   │
│  📈 Grafana:               localhost:3000 (admin/admin)     │
│  🔎 Zipkin:                localhost:9411                   │
│  🎛️  Kafka UI:              localhost:8090                   │
└─────────────────────────────────────────────────────────────┘
```

---

**Welcome to the team! 🎉**

If you can't find what you're looking for, ask in `#ecommerce-backend` Slack channel.
