# 🎯 Lộ Trình Đọc Hiểu Source Code E-commerce Backend

Tài liệu này hướng dẫn bạn tiếp cận và hiểu sâu source code dự án theo từng giai đoạn, từ tổng quan đến chi tiết.

---

## 📋 Tổng Quan Dự Án

| Thông tin | Chi tiết |
|-----------|----------|
| **Ngôn ngữ** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 3.2 |
| **Kiến trúc** | Hexagonal Architecture + Microservices |
| **Database** | PostgreSQL (Database per Service) |
| **Messaging** | Apache Kafka |
| **Patterns** | Saga, Outbox, CQRS (lite), Event-Driven |

---

## 🗺️ Lộ Trình 5 Giai Đoạn

```
┌─────────────────────────────────────────────────────────────────┐
│  PHASE 1: Tổng Quan & Kiến Trúc (2-3 giờ)                       │
│  → Hiểu big picture, các services và cách chúng giao tiếp      │
├─────────────────────────────────────────────────────────────────┤
│  PHASE 2: Common Library (1-2 giờ)                              │
│  → Hiểu các building blocks dùng chung                         │
├─────────────────────────────────────────────────────────────────┤
│  PHASE 3: Services Đơn Giản (3-4 giờ)                           │
│  → Product Service, Inventory Service                          │
├─────────────────────────────────────────────────────────────────┤
│  PHASE 4: Order Service - Core Brain (4-6 giờ)                  │
│  → Saga Pattern, Outbox Pattern, Event Consumption             │
├─────────────────────────────────────────────────────────────────┤
│  PHASE 5: Supporting Services (2-3 giờ)                         │
│  → Payment, Notification, Auth, API Gateway                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔵 PHASE 1: Tổng Quan & Kiến Trúc

**Mục tiêu**: Hiểu toàn cảnh hệ thống trước khi đi vào chi tiết.

### 1.1 Đọc Tài Liệu Kiến Trúc

| Thứ tự | File | Nội dung chính |
|--------|------|----------------|
| 1 | [docs/architecture/system-overview.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/system-overview.md) | Kiến trúc tổng thể, tech stack |
| 2 | [docs/services/README.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/services/README.md) | Service catalog, ports, dependencies |
| 3 | [docs/architecture/data-model.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/data-model.md) | Database schemas, relationships |

### 1.2 Hiểu Cấu Trúc Thư Mục

```
ecommerce-backend/
├── common-lib/           ← Thư viện dùng chung (Outbox, Security, Exceptions)
├── api-gateway/          ← Entry point, routing, JWT validation
├── auth-service/         ← User management, JWT generation
├── product-service/      ← Catalog management (đơn giản nhất)
├── inventory-service/    ← Stock management + Kafka consumer
├── order-service/        ← 🧠 CORE: Saga orchestration
├── payment-service/      ← Payment processing + Kafka
├── notification-service/ ← Email/SMS notifications
└── docker/               ← Docker Compose infrastructure
```

### 1.3 Câu Hỏi Cần Trả Lời

> 💡 **Tip AI**: Hỏi AI "Explain the flow when a customer creates an order in this system"

- [ ] Có bao nhiêu microservices? Chức năng chính của từng service?
- [ ] Services giao tiếp với nhau bằng cách nào? (HTTP vs Kafka)
- [ ] Database strategy là gì? (Shared vs Per-service)
- [ ] Hexagonal Architecture được tổ chức như thế nào trong code?

---

## 🟢 PHASE 2: Common Library

**Mục tiêu**: Hiểu các building blocks được reuse across services.

### 2.1 Cấu Trúc Common-Lib

```
common-lib/src/main/java/com/ecommerce/common/
├── domain/          ← Base classes (AggregateRoot, DomainEvent)
├── outbox/          ← Transactional Outbox pattern implementation
├── security/        ← JWT validation, Security config
├── exception/       ← Global exception handling
├── config/          ← Shared configurations
└── event/           ← Domain event interfaces
```

### 2.2 Thứ Tự Đọc Code

| Thứ tự | Package | Files quan trọng | Học được gì |
|--------|---------|------------------|-------------|
| 1 | `domain` | `AggregateRoot.java`, `DomainEvent.java` | DDD base classes |
| 2 | `outbox` | `OutboxEvent.java`, `OutboxPublisher.java` | Transactional Outbox pattern |
| 3 | `security` | `JwtUtils.java`, `SecurityConfig.java` | JWT parsing, Spring Security |
| 4 | `exception` | `GlobalExceptionHandler.java` | Error handling strategy |

### 2.3 Câu Hỏi Cần Trả Lời

> 💡 **Tip AI**: "Explain how the Outbox pattern is implemented in common-lib"

- [ ] `AggregateRoot` làm gì? Tại sao cần nó?
- [ ] Outbox pattern hoạt động như thế nào? (DB transaction → Kafka publish)
- [ ] JWT token được validate ở đâu và như thế nào?

---

## 🟡 PHASE 3: Services Đơn Giản

**Mục tiêu**: Hiểu cấu trúc service tiêu chuẩn qua các service đơn giản.

### 3.1 Product Service (Đơn giản nhất)

**Tại sao bắt đầu từ đây**: Không có Kafka, chỉ có CRUD operations.

```
product-service/src/main/java/com/ecommerce/product/
├── domain/
│   ├── model/         ← Product.java (Entity)
│   ├── repository/    ← ProductRepository.java (Interface - PORT)
│   └── service/       ← ProductDomainService.java
├── application/
│   ├── service/       ← ProductApplicationService.java (Use cases)
│   └── dto/           ← Request/Response DTOs
└── infrastructure/
    ├── persistence/   ← ProductJpaRepository.java (ADAPTER)
    ├── web/           ← ProductController.java
    └── config/        ← OpenAPIConfig.java
```

**Thứ tự đọc**:
1. `domain/model/Product.java` - Entity và business logic
2. `domain/repository/ProductRepository.java` - Port (interface)
3. `infrastructure/persistence/` - Adapter (JPA implementation)
4. `application/service/ProductApplicationService.java` - Use cases
5. `infrastructure/web/ProductController.java` - REST API

### 3.2 Inventory Service (Thêm Kafka Consumer)

**Tại sao tiếp theo**: Giới thiệu Kafka consumption + Optimistic Locking.

**Focus vào**:
- `InventoryEventConsumer.java` - Cách consume Kafka events
- `Inventory.java` - Optimistic locking với `@Version`
- `StockReservationService.java` - Business logic reserve/release

### 3.3 Câu Hỏi Cần Trả Lời

> 💡 **Tip AI**: "Show me the Hexagonal Architecture layers in product-service"

- [ ] Domain layer có dependency gì không? (Hint: Không có!)
- [ ] Repository interface nằm ở đâu? Implementation nằm ở đâu?
- [ ] Inventory Service xử lý concurrent stock updates như thế nào?
- [ ] Khi có `OrderCreated` event, Inventory Service làm gì?

---

## 🔴 PHASE 4: Order Service - Core Brain

**Mục tiêu**: Hiểu sâu Saga Pattern và Event-Driven Architecture.

### 4.1 Tổng Quan Order Service

```
order-service/src/main/java/com/ecommerce/order/
├── domain/
│   ├── model/         ← Order.java, OrderItem.java, OrderSaga.java
│   ├── event/         ← OrderCreated.java, OrderConfirmed.java
│   ├── repository/    ← OrderRepository.java
│   └── service/       ← OrderDomainService.java
├── application/
│   ├── service/       ← OrderApplicationService.java
│   ├── saga/          ← 🔥 OrderSagaOrchestrator.java
│   └── dto/
└── infrastructure/
    ├── kafka/         ← EventConsumer.java, EventPublisher.java
    ├── persistence/   ← OrderJpaRepository.java
    ├── outbox/        ← OutboxEventPublisher.java
    └── web/           ← OrderController.java
```

### 4.2 Thứ Tự Đọc (QUAN TRỌNG!)

| Thứ tự | Component | File | Học được gì |
|--------|-----------|------|-------------|
| 1 | Domain Model | `Order.java` | Order aggregate, state transitions |
| 2 | Domain Events | `domain/event/*.java` | Event definitions |
| 3 | Saga State | `OrderSaga.java` | Saga state machine |
| 4 | **Saga Orchestrator** | `OrderSagaOrchestrator.java` | 🔥 Core logic |
| 5 | Event Consumer | `InventoryEventConsumer.java` | React to external events |
| 6 | Outbox Publisher | `OutboxEventPublisher.java` | Reliable publishing |

### 4.3 Saga Flow Chi Tiết

```
Customer → POST /orders
    ↓
Order Service: Create Order (PENDING)
    ↓
Outbox: Save OrderCreated event
    ↓
Poller: Publish to Kafka (order-events)
    ↓
Inventory Service: Consume OrderCreated
    ↓
Inventory Service: Reserve stock → Publish AllItemsReserved/StockReservationFailed
    ↓
Order Service: Consume AllItemsReserved
    ↓
Order Service: Request payment → Publish PaymentRequested
    ↓
Payment Service: Process payment → Publish PaymentCompleted/PaymentFailed
    ↓
Order Service: Consume PaymentCompleted
    ↓
Order Service: Confirm order (CONFIRMED)
```

### 4.4 Câu Hỏi Cần Trả Lời

> 💡 **Tip AI**: "Trace the complete flow of an order from creation to confirmation"

- [ ] Order có những states nào? Transitions hợp lệ là gì?
- [ ] `OrderSagaOrchestrator` xử lý saga như thế nào?
- [ ] Nếu Payment fails, compensation flow hoạt động ra sao?
- [ ] Outbox pattern đảm bảo at-least-once delivery như thế nào?
- [ ] Idempotency được implement ở đâu?

### 4.5 Tài Liệu Hỗ Trợ

- [Order Saga Flow](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/order-saga-flow.md)
- [Outbox Pattern](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/outbox-pattern.md)
- [Event Catalog](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/event-catalog.md)

---

## 🟣 PHASE 5: Supporting Services

**Mục tiêu**: Hoàn thiện bức tranh với các services còn lại.

### 5.1 Payment Service

**Focus**: Kafka consumer + producer, simulated payment processing.

- `PaymentEventConsumer.java` - Consume `PaymentRequested`
- `PaymentProcessor.java` - Business logic (simulated)
- `OutboxEventPublisher.java` - Publish `PaymentCompleted/Failed`

### 5.2 Notification Service

**Focus**: Event-driven notifications, no REST API.

- `NotificationEventConsumer.java` - Consume order/payment events
- `NotificationService.java` - Email/SMS logic

### 5.3 Auth Service

**Focus**: JWT generation, user management.

- `AuthenticationService.java` - Login, register, refresh
- `JwtTokenProvider.java` - Token generation
- `User.java` - User entity with roles

### 5.4 API Gateway

**Focus**: Routing, rate limiting, JWT validation.

- `GatewayConfig.java` - Route definitions
- `JwtAuthenticationFilter.java` - Token validation
- `RateLimitingFilter.java` - Rate limiting logic

---

## 🤖 Cách Sử Dụng AI Hiểu Code

### Prompt Templates

**1. Trace Flow**:
```
Trace the complete flow when a customer creates an order with 2 items.
Show me what happens in each service, which events are published,
and how the saga progresses through its states.
```

**2. Explain Pattern**:
```
Explain how the Transactional Outbox pattern is implemented in this codebase.
Show me the key classes involved and how they work together.
```

**3. Compare Implementations**:
```
Compare how Product Service and Inventory Service handle their domain logic.
What's the difference in complexity and why?
```

**4. Debug Scenario**:
```
If a PaymentFailed event is received, walk me through the compensation flow.
Which services are involved and what compensating actions are taken?
```

**5. Understand Tests**:
```
Show me how integration tests are structured in order-service.
How do they test the Saga flow?
```

---

## ✅ Checklist Hoàn Thành

### Phase 1: Tổng Quan
- [ ] Hiểu được 7 microservices và chức năng chính
- [ ] Hiểu được Hexagonal Architecture layers
- [ ] Hiểu được communication patterns (HTTP vs Kafka)

### Phase 2: Common Library
- [ ] Hiểu được Outbox pattern implementation
- [ ] Hiểu được JWT security flow
- [ ] Hiểu được base domain classes

### Phase 3: Simple Services
- [ ] Đọc xong Product Service code
- [ ] Hiểu được Kafka consumer trong Inventory Service
- [ ] Hiểu được Optimistic Locking

### Phase 4: Order Service
- [ ] Hiểu được Order aggregate và state machine
- [ ] Hiểu được Saga Orchestrator logic
- [ ] Trace được complete order flow
- [ ] Hiểu được compensation flow

### Phase 5: Supporting Services
- [ ] Hiểu được Payment processing flow
- [ ] Hiểu được Notification event consumption
- [ ] Hiểu được Auth/JWT generation
- [ ] Hiểu được API Gateway routing

---

## 📚 Tài Liệu Tham Khảo

| Chủ đề | Tài liệu |
|--------|----------|
| System Architecture | [system-overview.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/system-overview.md) |
| Database Schemas | [data-model.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/data-model.md) |
| Order Saga | [order-saga-flow.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/order-saga-flow.md) |
| Outbox Pattern | [outbox-pattern.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/outbox-pattern.md) |
| All Kafka Events | [event-catalog.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/architecture/event-catalog.md) |
| Security | [SECURITY.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/SECURITY.md) |
| Glossary | [GLOSSARY.md](file:///d:/Develop/MySelf/ecommerce/ecommerce-backend/docs/GLOSSARY.md) |

---

**Ước tính thời gian**: 12-18 giờ cho toàn bộ roadmap  
**Cách tiếp cận**: Đọc docs trước → Trace code flow → Debug locally → Hỏi AI khi stuck
