# 🧭 MASTER PLAN - E-commerce Backend

## Project Vision

Modern e-commerce backend built with **Java 21** and **Spring Boot 3.5.9** following microservice architecture.

---

## 📋 Phase Overview

| Phase | Focus | Status |
|-------|-------|--------|
| **Phase 1** | Core Domain (Product, Inventory, Order) | ✅ Complete |
| **Phase 2** | Distributed Flow (Payment, Notification, Kafka) | ✅ Mostly Complete |
| **Phase 3** | Enterprise (Security, Gateway, Observability) | 🔜 Planned |

---

## 🏗️ Architecture

### Style
- Microservice Architecture
- Hexagonal (Ports & Adapters) per service
- Event-driven (Phase 2)
- Cloud-native mindset

### Services
```
api-gateway          (Phase 3)
auth-service         (Phase 3)
product-service      ✅ Implemented
inventory-service    ✅ Implemented
order-service        ✅ Implemented
payment-service      (Phase 2)
notification-service (Phase 2)
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
| Messaging | Kafka (Phase 2) |
| Migration | Flyway |

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

### Topics
- JWT + OAuth2 authentication
- API Gateway with rate limiting
- Virtual Threads vs Thread Pool benchmarks
- Redis caching
- Observability (tracing, metrics)

---

## 📁 Project Structure

```
ecommerce-platform/
├── common-lib/           # Shared code
├── product-service/      # Port 8081
├── inventory-service/    # Port 8082
├── order-service/        # Port 8083
├── docker/               # Infrastructure
├── docs/                 # Documentation
└── build.gradle.kts      # Root build
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
