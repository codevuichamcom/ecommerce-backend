# Modern E-commerce Backend

A microservice-based e-commerce platform built with **Java 21** and **Spring Boot 3.2**.

## 🏗️ Architecture

The platform follows a modern, distributed architecture designed for scalability and reliability.

- **Hexagonal Architecture** (Ports & Adapters) per service
- **Domain-Driven Design (DDD)** core principles
- **Event-Driven Architecture** using the **Saga Pattern**
- **Transactional Outbox Pattern** for reliable messaging

Detailed documentation:
- [System Overview](docs/architecture/system-overview.md)
- [Order Creation Saga Flow](docs/architecture/order-saga-flow.md)
- [Transactional Outbox Mechanism](docs/architecture/outbox-pattern.md)

## 🚀 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.2.5 |
| Build | Gradle 8.7 (Kotlin DSL) |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Messaging | Kafka (Phase 2) |

## 📦 Services

| Service | Port | Description |
|---------|------|-------------|
| product-service | 8081 | Product catalog management |
| inventory-service | 8082 | Stock & reservation management |
| order-service | 8083 | Order processing |

## 🛠️ Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.7+ (or use wrapper)

## 🏃 Quick Start

```bash
# Start infrastructure
cd docker
docker-compose up -d

# Build all services
./gradlew build

# Run individual services
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :order-service:bootRun
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run integration tests (requires Docker)
./gradlew integrationTest
```

## ☕ Java 21 Features Used

- **Records** - Immutable DTOs, Commands, Value Objects
- **Sealed Classes** - Type-safe state machines
- **Pattern Matching** - Exhaustive switch expressions
- **Virtual Threads** - Scalable request handling

## 📁 Project Structure

```
ecommerce-platform/
├── common-lib/          # Shared utilities
├── product-service/     # Product catalog
├── inventory-service/   # Stock management
├── order-service/       # Order processing
├── docker/              # Docker configs
└── build.gradle.kts     # Root build
```

## 📄 License

MIT License
