# 🧭 MASTER PLAN - E-commerce Backend

## Project Vision

Modern e-commerce backend built with **Java 21** and **Spring Boot 3.5.9** following microservice architecture.

---

## 📋 Phase Overview

| Phase | Focus | Status |
|-------|-------|--------|
| **Phase 1** | Core Domain (Product, Inventory, Order) | ✅ Complete |
| **Phase 2** | Distributed Flow (Payment, Notification, Kafka) | ✅ Complete |
| **Phase 3** | Enterprise (Security, Gateway, Observability) | 🔄 In Progress (~80%) |
| **Phase 4** | Resilience & Testing (Circuit Breaker, Chaos) | 🔜 Planned |

---

## 🏗️ Architecture

### Style
- Microservice Architecture
- Hexagonal (Ports & Adapters) per service
- Event-driven (Phase 2)
- Cloud-native mindset

### Services
```
api-gateway          ✅ Port 8080
auth-service         ✅ Port 8086
product-service      ✅ Port 8081
inventory-service    ✅ Port 8082
order-service        ✅ Port 8083
payment-service      ✅ Port 8084
notification-service ✅ Port 8085
```

---

## 🔧 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5.9 |
| Build | Gradle 8.12 (Kotlin DSL) |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Messaging | Apache Kafka 7.5 |
| Migration | Flyway |
| Gateway | Spring Cloud Gateway (Phase 3) |
| Security | JWT + Spring Security (Phase 3) |
| Tracing | Micrometer Tracing + Zipkin (Phase 3) |
| Metrics | Prometheus + Grafana (Phase 3) |

---

## 🔥 Java 21 Features (Required)

### Language
- `record` → DTOs, Commands, Value Objects
- `sealed class` → State machines, Result types
- Pattern Matching → Exhaustive switch
- Switch expressions

### Concurrency
- **Virtual Threads** for REST APIs
- Structured Concurrency (preview)

### Style
- Immutable objects
- Explicit boundaries
- No shared mutable state

---

## 📚 Phase 1: Core Domain

### Problems Solved

1. **Inventory Consistency**
   - Optimistic locking with `@Version`
   - `@Retryable` for transient failures
   - No overselling

2. **Order Creation Flow**
   - Idempotent API (`Idempotency-Key` header)
   - Multi-layer validation
   - Automatic rollback on partial failure

3. **JPA Best Practices**
   - Separate domain/JPA entities (no leak)
   - Fetch joins to avoid N+1
   - Flyway migrations

---

## 🚀 Phase 2: Distributed Flow (Next)

### Topics
- Saga pattern for distributed transactions
- Kafka for event publishing
- Outbox pattern
- Idempotent consumers

---

## 🏢 Phase 3: Enterprise Level

### New Services
- `api-gateway` (Port 8080) - Single entry point, routing, rate limiting
- `auth-service` (Port 8086) - JWT authentication, user management

### Core Features
1. **API Gateway** (Spring Cloud Gateway)
   - Central routing to all services
   - JWT validation at gateway level
   - Rate limiting (Redis-backed)
   - Request/response logging

2. **Authentication** (JWT-based, simplified)
   - Login → JWT access token + refresh token
   - Role-based access (ADMIN, CUSTOMER, SERVICE)
   - No full OAuth2 server (avoid over-engineering)

3. **Observability**
   - Distributed tracing (Micrometer Tracing + Zipkin)
   - Metrics (Micrometer + Prometheus + Grafana)
   - Structured JSON logging with correlation IDs

4. **Caching**
   - Redis cache for product catalog
   - @Cacheable annotations

### Out of Scope (Keep Simple)
- ❌ Service Mesh (Istio) - overkill for 7 services
- ❌ Full OAuth2 Authorization Server - JWT đủ dùng
- ❌ ELK Stack - Grafana Loki nếu cần sau
- ❌ mTLS everywhere - chỉ production

See [PHASE3_IMPLEMENTATION_PLAN.md](./PHASE3_IMPLEMENTATION_PLAN.md) for details.

---

## 📁 Project Structure

```
ecommerce-backend/
├── common-lib/           # Shared code
├── api-gateway/          # Port 8080 (Phase 3)
├── auth-service/         # Port 8086 (Phase 3)
├── product-service/      # Port 8081
├── inventory-service/    # Port 8082
├── order-service/        # Port 8083
├── payment-service/      # Port 8084 (Phase 2)
├── notification-service/ # Port 8085 (Phase 2)
├── docker/
│   ├── docker-compose.yml
│   ├── prometheus/       # Phase 3
│   └── grafana/          # Phase 3
├── docs/
└── build.gradle.kts
```

---

## ✅ Quick Start

```bash
# Start infrastructure
cd docker && docker-compose up -d

# Build
./gradlew build

# Run services
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :order-service:bootRun
```
