# 🛠️ System Refinement Plan

This document tracks the final refinements and critical bug fixes based on the project audit. These issues are essential for production readiness, ensuring data consistency, and system resilience.

## 📋 Task Overview

| ID | Issue | Priority | Status | Service(s) |
|:---|:---|:---:|:---:|:---|
| **REF-01** | Kafka Exception Swallowing (No Retry/DLQ) | 🔥 High | ✅ Complete | Payment, Inventory, Notification |
| **REF-02** | Outbox Table Name Mismatch | 🔥 High | ✅ Complete | Order |
| **REF-03** | Saga: Multi-item Inventory Support | 🛡️ Med | ✅ Complete | Inventory |
| **REF-04** | Sync Calls: Missing WebClient Resilience | 🛡️ Med | ✅ Complete | Order |
| **REF-05** | Gateway: Environment Config & Security | 🛡️ Med | ✅ Complete | API Gateway |
| **REF-06** | Consumer Idempotency Race Condition | 🛡️ Med | ✅ Complete | Common-lib |
| **REF-07** | Kafka Naming Consistency | 📎 Low | ✅ Complete | Order, Common-lib |

---

## 🔍 Detailed Implementation Tasks

### 1. Kafka Exception Swallowing (REF-01)
- **Problem**: Consumers catch exceptions and only log them, leading to offset commitment on failure.
- **Tasks**:
    - [ ] Remove universal `try-catch` in `@KafkaListener` methods that swallow exceptions.
    - [ ] Ensure exceptions are re-thrown to trigger Spring Kafka retry mechanism.
    - [ ] (Optional) Configure `DefaultErrorHandler` with Dead Letter Topics (DLT).
- **Target Files**:
    - `payment-service/.../kafka/PaymentEventConsumer.java`
    - `inventory-service/.../kafka/InventoryEventConsumer.java`
    - `notification-service/.../kafka/NotificationEventConsumer.java`

### 2. Outbox Table Mismatch (REF-02)
- **Problem**: Migration creates `outbox_messages` but repository queries `outbox_events`.
- **Tasks**:
    - [ ] Update `OutboxJpaRepository.java` native query to use `outbox_messages`.
    - [ ] Verify `OutboxEventEntity` `@Table` annotation matches (currently `outbox_messages`).
- **Target Files**:
    - `order-service/.../persistence/repository/OutboxJpaRepository.java`

### 3. Saga: Multi-item Inventory (REF-03)
- **Problem**: `InventoryService` only processes the first item in an order during reservation.
- **Tasks**:
    - [ ] Update `handleOrderCreated` to iterate through all items in `itemsNode`.
    - [ ] Ensure atomicity: If one item fails to reserve, roll back/cancel others (or trigger compensation).
- **Target Files**:
    - `inventory-service/.../application/service/InventoryService.java`

### 4. WebClient Resilience (REF-04)
- **Problem**: Synchronous `.block()` calls lack timeouts or circuit breakers.
- **Tasks**:
    - [ ] Add `.timeout(Duration.ofSeconds(x))` to all `WebClient` calls.
    - [ ] Implement retry or fallback logic for non-critical calls.
- **Target Files**:
    - `order-service/.../client/ProductServiceClient.java`
    - `order-service/.../client/InventoryServiceClient.java`

### 5. Gateway Security & Config (REF-05)
- **Problem**: Hardcoded `localhost` routes and `*` CORS policy.
- **Tasks**:
    - [ ] Move route URIs to `application.yml` using environment variables (e.g., `${SERVICE_PRODUCT_URL}`).
    - [ ] Restrict `allowedOrigins` to specific domains (or internal dev URLs).
- **Target Files**:
    - `api-gateway/.../config/RouteConfig.java`
    - `api-gateway/src/main/resources/application.yml`

### 6. Idempotency Race Condition (REF-06)
- **Problem**: `existsBy...` followed by `save(...)` isn't atomic under high load.
- **Tasks**:
    - [ ] Rely on DB Unique Constraint on `eventId`.
    - [ ] Catch `DataIntegrityViolationException` in `IdempotentEventHandler` to handle duplicates gracefully.
- **Target Files**:
    - `common-lib/.../kafka/IdempotentEventHandler.java`

### 7. Naming Consistency (REF-07)
- **Problem**: Inconsistent `groupId` and hardcoded strings.
- **Tasks**:
    - [ ] Use `KafkaTopics.ORDER_SERVICE_GROUP` constant in `@KafkaListener`.
- **Target Files**:
    - `order-service/.../kafka/OrderEventConsumer.java`

---

## 🚀 Execution Strategy

1. **Phase 1 (Reliability)**: Fix REF-01, REF-02, and REF-06.
2. **Phase 2 (Logic)**: Fix REF-03 and REF-04.
3. **Phase 3 (Operational)**: Fix REF-05 and REF-07.
