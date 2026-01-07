# 🛒 Modern E-commerce Microservices Platform

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://jdk.java.net/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)](https://spring.io/projects/spring-boot)
[![Graduate](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet.svg)](#architecture)

A high-performance, scalable E-commerce backend built with **Java 21**, **Spring Boot**, and **PostgreSQL**. The system demonstrates advanced distributed system patterns including **Event-Driven Architecture**, **Saga Pattern**, and **Transactional Outbox**.

---

## 🏗️ Architecture Design

We move beyond simple CRUD. This system handles failure scenarios, distributed transactions, and high concurrency using industry-standard patterns.

*   **Hexagonal Architecture**: Clean separation of Domain, Application, and Infrastructure layers.
*   **Transactional Outbox**: Guaranteed event delivery (bye-bye distributed transactions).
*   **Saga Pattern**: Orchestrated workflows for complex operations like "Order Creation".
*   **Idempotency**: Safe retry mechanisms at API and Event Consumer levels.

### 📚 Documentation

Dive deep into the engineering decisions:

*   [**System Overview**](docs/architecture/system-overview.md) - The High Level Design (HLD) and Container Diagrams.
*   [**Order Saga Flow**](docs/architecture/order-saga-flow.md) - How we coordinate Order, Inventory, and Payment services.
*   [**Outbox Pattern**](docs/architecture/outbox-pattern.md) - How we solve the dual-write problem.

---

## ⚡ Key Features

*   **Latest Tech Stack**: Java 21 LTS, Virtual Threads (Project Loom), Records, Pattern Matching.
*   **Performance**: Optimistic Locking for inventory, Redis for caching, PostgreSQL SKIP LOCKED for event polling.
*   **Reliability**: Comprehensive test suite (Unit, Integration) using TestContainers.

---

## 🚀 Getting Started

### Prerequisites
*   Docker & Docker Compose
*   Java 21 SDK
*   Gradle 8+

### 1️⃣ Start Infrastructure
Spin up PostgreSQL, Redis, and Kafka in containers.
```bash
cd docker
docker-compose up -d
```

### 2️⃣ Build Services
Compile and run tests.
```bash
./gradlew build
```

### 3️⃣ Run Services
Start the microservices (in separate terminals).
```bash
# Terminal 1: Core
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun

# Terminal 2: Orchestration & Utils
./gradlew :payment-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :order-service:bootRun
```

---

## 📦 Service Catalog

| Service | Port | Responsibility |
| :--- | :--- | :--- |
| **Product Service** | `8081` | Catalog management, Price lookups. |
| **Inventory Service** | `8082` | Stock management, Optimistic reservation. |
| **Order Service** | `8083` | Process Orchestrator (Saga), State machine. |
| **Payment Service** | `8084` | Payment processing gateway. |
| **Notification** | `8085` | Email & Push notification handler. |

---

## 🧪 Testing

We take quality seriously.

```bash
# Run Fast Unit Tests
./gradlew test

# Run Integration Tests (Requires Docker)
./gradlew integrationTest
```

---

## � Project Structure

```
ecommerce-platform/
├── common-lib/             # Shared Domain Events, Exception Handling, Outbox Logic
├── product-service/        # Domain: Catalog
├── inventory-service/      # Domain: Stock
├── order-service/          # Domain: Order Lifecycle (The brain)
├── payment-service/        # Domain: Finance
├── notification-service/   # Domain: Communication
├── docker/                 # Infrastructure as Code
└── docs/                   # Architecture & Design Docs
```

---

## 📄 License
MIT License
