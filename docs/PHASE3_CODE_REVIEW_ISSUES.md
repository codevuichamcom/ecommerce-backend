# Phase 3 Code Review Issues

**Review Date:** 2026-01-18
**Reviewer:** Senior Java Backend Developer
**Status:** Pending Fixes

---

## Severity Legend

| Icon | Level | Description |
|------|-------|-------------|
| :red_circle: | Critical | Security vulnerabilities, data loss risk, must fix before production |
| :orange_circle: | Major | Significant bugs, architecture violations, should fix soon |
| :yellow_circle: | Minor | Code quality, best practices, can fix later |
| :blue_circle: | Suggestion | Nice-to-have improvements |

---

## Summary

| Category | Critical | Major | Minor | Total |
|----------|----------|-------|-------|-------|
| Security | 2 | 3 | 0 | 5 |
| Architecture | 0 | 4 | 1 | 5 |
| Concurrency | 0 | 2 | 1 | 3 |
| Code Quality | 0 | 2 | 4 | 6 |
| Saga Pattern | 0 | 1 | 1 | 2 |
| Domain Model | 0 | 0 | 2 | 2 |
| Observability | 0 | 0 | 2 | 2 |
| **TOTAL** | **2** | **12** | **11** | **25** |

---

## 1. Security Issues

### SEC-001 :red_circle: JWT Secret Key Validation Missing

**File:** `api-gateway/src/main/java/com/ecommerce/gateway/filter/JwtAuthenticationFilter.java:45-46`

**Current Code:**
```java
public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

**Problem:**
- No validation for minimum secret key length (HS256 requires at least 256 bits = 32 bytes)
- JJWT will throw runtime exception if secret is too short
- No fail-fast mechanism at startup

**Recommendation:**
```java
public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
        throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
    }
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

**Status:** [ ] Not Started

---

### SEC-002 :red_circle: HTTP Header Injection Vulnerability

**File:** `api-gateway/src/main/java/com/ecommerce/gateway/filter/JwtAuthenticationFilter.java:80-84`

**Current Code:**
```java
ServerHttpRequest mutatedRequest = request.mutate()
        .header("X-User-Id", userId)
        .header("X-User-Name", username)
        .header("X-User-Roles", String.join(",", roles.stream().map(Object::toString).toList()))
        .build();
```

**Problem:**
- JWT claims are not validated/sanitized before being put into HTTP headers
- Attacker could craft JWT with username containing newline characters (`\r\n`)
- Could lead to HTTP Header Injection attacks

**Recommendation:**
```java
private String sanitizeHeaderValue(String value) {
    if (value == null) return "";
    return value.replaceAll("[\\r\\n]", "");
}

ServerHttpRequest mutatedRequest = request.mutate()
        .header("X-User-Id", sanitizeHeaderValue(userId))
        .header("X-User-Name", sanitizeHeaderValue(username))
        .header("X-User-Roles", sanitizeHeaderValue(String.join(",", roles...)))
        .build();
```

**Status:** [ ] Not Started

---

### SEC-003 :orange_circle: Missing Input Validation for User Registration

**File:** `auth-service/src/main/java/com/ecommerce/auth/application/service/AuthService.java:48-64`

**Problem:**
- No password strength validation (length, complexity)
- No email format validation
- No username format validation (could contain special characters)
- No rate limiting for registration endpoint

**Recommendation:**
- Add password policy: minimum 8 chars, uppercase, lowercase, number, special char
- Validate email with regex or library
- Validate username: alphanumeric, 3-30 chars
- Add rate limiting at API Gateway level

**Status:** [ ] Not Started

---

### SEC-004 :orange_circle: NPE Risk in Rate Limiting

**File:** `api-gateway/src/main/java/com/ecommerce/gateway/config/RateLimitConfig.java:27-28`

**Current Code:**
```java
return Mono.just(
    Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
```

**Problem:**
- `getRemoteAddress()` can return `null` when request comes from proxy without proper header forwarding
- `Objects.requireNonNull` will throw NPE in production

**Recommendation:**
```java
return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
    .map(addr -> addr.getAddress().getHostAddress())
    .defaultIfEmpty("unknown");
```

**Status:** [ ] Not Started

---

### SEC-005 :orange_circle: Timing Attack Vulnerability in Login

**File:** `auth-service/src/main/java/com/ecommerce/auth/application/service/AuthService.java:67-73`

**Current Code:**
```java
User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new ValidationException("Invalid username or password"));

if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
    throw new ValidationException("Invalid username or password");
}
```

**Problem:**
- When username doesn't exist, response time is shorter (no BCrypt comparison)
- Attacker can enumerate valid usernames by measuring response times

**Recommendation:**
```java
private static final String DUMMY_HASH = "$2a$10$dummyhashfordummycomparison";

User user = userRepository.findByUsername(request.username())
        .orElseGet(() -> {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw new ValidationException("Invalid username or password");
        });
```

**Status:** [ ] Not Started

---

## 2. Architecture Issues

### ARCH-001 :orange_circle: DDD Violation - Infrastructure Manipulating Domain

**File:** `order-service/src/main/java/com/ecommerce/order/infrastructure/kafka/OrderEventConsumer.java:141-143`

**Current Code:**
```java
orderRepository.findById(orderId).ifPresent(order -> {
    order.confirm();
    orderRepository.save(order);
```

**Problem:**
- Infrastructure layer (Kafka Consumer) directly manipulates domain model
- Violates Hexagonal Architecture - Infrastructure should not know about Domain logic

**Recommendation:**
Move logic to Application Service:
```java
// In OrderService
public void onPaymentCompleted(String orderId) {
    Order order = findOrderOrThrow(orderId);
    order.confirm();
    orderRepository.save(order);
    publishOrderConfirmed(order);
}

// In OrderEventConsumer
orderService.onPaymentCompleted(event.orderId());
```

**Status:** [ ] Not Started

---

### ARCH-002 :orange_circle: Transaction Boundary Issue in PaymentService

**File:** `payment-service/src/main/java/com/ecommerce/payment/application/service/PaymentService.java:58-89`

**Current Code:**
```java
public PaymentResponse processPayment(ProcessPaymentCommand command) {
    Payment payment = initializePayment(command);  // @Transactional
    simulatePaymentProcessing();  // Thread.sleep - outside transaction
    payment = completePayment(payment.getId());  // @Transactional
}
```

**Problems:**
1. `processPayment` method is NOT `@Transactional` but calls 3 transactional methods
2. Payment state can be corrupted if server crashes between `initializePayment` and `completePayment`
3. **Self-invocation in same class doesn't trigger proxy** - `@Transactional` on sub-methods won't work!

**Recommendation:**
- Extract transactional methods to separate bean
- Or use saga state machine for payment processing
- Or use programmatic transaction management

**Status:** [ ] Not Started

---

### ARCH-003 :orange_circle: Incomplete Saga Compensation

**File:** `inventory-service/src/main/java/com/ecommerce/inventory/application/service/InventoryService.java:212-241`

**Current Code:**
```java
public void handleOrderCancelled(String orderId) {
    log.warn("Stock release for OrderCancelled {} not fully implemented without ProductID lookup", orderId);
}
```

**Problem:**
- Compensation logic for OrderCancelled is not implemented
- Critical part of Saga pattern - if stock is not released on order cancel, leads to stock leak

**Recommendation:**
1. Add `findByReservationRef(String orderId)` to InventoryRepository
2. Or include items in OrderCancelled event
3. Or store reservation mapping table

**Status:** [ ] Not Started

---

### ARCH-004 :orange_circle: Static ObjectMapper Instance

**File:** `common-lib/src/main/java/com/ecommerce/common/outbox/OutboxEventPublisher.java:19`

**Current Code:**
```java
private static final ObjectMapper objectMapper = createObjectMapper();
```

**Problem:**
- ObjectMapper is thread-safe but static instance in library class causes issues when customization is needed per-service

**Recommendation:**
Inject ObjectMapper through constructor:
```java
private final ObjectMapper objectMapper;

public OutboxEventPublisher(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
}
```

**Status:** [ ] Not Started

---

### ARCH-005 :yellow_circle: Missing Database Index

**File:** `order-service/src/main/java/com/ecommerce/order/infrastructure/persistence/adapter/OrderPersistenceAdapter.java:47-50`

**Problem:**
- Query by `customerId` is frequently called but no index definition visible in JPA entity
- Ensure index exists on `customer_id` column

**Recommendation:**
Add to OrderJpaEntity:
```java
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
    @Index(name = "idx_orders_idempotency_key", columnList = "idempotency_key")
})
```

**Status:** [ ] Not Started

---

## 3. Concurrency Issues

### CONC-001 :orange_circle: Race Condition in Idempotency Check

**File:** `order-service/src/main/java/com/ecommerce/order/application/service/OrderService.java:57-63`

**Current Code:**
```java
if (idempotencyKey != null && !idempotencyKey.isBlank()) {
    var existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
    if (existingOrder.isPresent()) {
        return OrderResponse.from(existingOrder.get());
    }
}
// ... create order
```

**Problem:**
- TOCTOU (Time-of-check to time-of-use) race condition
- Two concurrent requests with same idempotencyKey can both pass check and create duplicates

**Recommendation:**
Option 1: Database unique constraint + exception handling
```java
try {
    // create order
} catch (DataIntegrityViolationException e) {
    return orderRepository.findByIdempotencyKey(idempotencyKey)
        .map(OrderResponse::from)
        .orElseThrow();
}
```

Option 2: Distributed lock (Redis SETNX)
```java
if (!redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofMinutes(5))) {
    // wait and fetch existing order
}
```

**Status:** [ ] Not Started

---

### CONC-002 :orange_circle: Thread.sleep in Service Layer

**File:** `payment-service/src/main/java/com/ecommerce/payment/application/service/PaymentService.java:214-220`

**Current Code:**
```java
private void simulatePaymentProcessing() {
    try {
        Thread.sleep(processingDelayMs);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

**Problem:**
- Blocking thread with sleep in production code is anti-pattern
- Especially problematic with Virtual Threads context
- Easy to misuse if called within transaction

**Recommendation:**
For simulation/testing only - use profile-based configuration:
```java
@Profile("!production")
private void simulatePaymentProcessing() { ... }
```

**Status:** [ ] Not Started

---

### CONC-003 :yellow_circle: Potential Deadlock in Outbox Polling

**File:** `common-lib/src/main/java/com/ecommerce/common/outbox/OutboxPoller.java:53-71`

**Problem:**
- `findUnpublishedForUpdate` uses SELECT FOR UPDATE
- Multiple instances polling same table could cause lock contention
- Consider using SKIP LOCKED for PostgreSQL

**Recommendation:**
```sql
SELECT * FROM outbox_events WHERE published = false
ORDER BY created_at
LIMIT :batch_size
FOR UPDATE SKIP LOCKED
```

**Status:** [ ] Not Started

---

## 4. Code Quality Issues

### CQ-001 :orange_circle: Swallowed Exceptions in Event Processing

**File:** `order-service/src/main/java/com/ecommerce/order/infrastructure/kafka/OrderEventConsumer.java:61-63`

**Current Code:**
```java
} catch (Exception e) {
    log.error("Failed to process inventory event: {}", e.getMessage());
}
```

**Problems:**
1. Exception is completely swallowed - message committed but event not processed
2. No retry logic
3. No dead-letter queue handling
4. Stack trace lost (only logs message)

**Recommendation:**
```java
} catch (Exception e) {
    log.error("Failed to process inventory event: {}", message, e);
    throw e; // Let Kafka consumer retry or move to DLQ
}
```

**Status:** [ ] Not Started

---

### CQ-002 :orange_circle: Brittle Event Type Detection

**File:** `order-service/src/main/java/com/ecommerce/order/infrastructure/kafka/OrderEventConsumer.java:52-57`

**Current Code:**
```java
if ("AllItemsReserved".equals(eventType) || message.contains("AllItemsReserved")) {
```

**Problems:**
1. `message.contains("AllItemsReserved")` is fragile - could match data field containing this string
2. Unknown event types not handled gracefully
3. String-based type checking instead of proper polymorphic deserialization

**Recommendation:**
Use `@JsonTypeInfo` for polymorphic deserialization:
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AllItemsReserved.class, name = "AllItemsReserved"),
    @JsonSubTypes.Type(value = StockReservationFailed.class, name = "StockReservationFailed")
})
public sealed interface InventoryEvents extends DomainEvent { ... }
```

**Status:** [ ] Not Started

---

### CQ-003 :yellow_circle: Generic Exception Catching in JWT Validation

**File:** `auth-service/src/main/java/com/ecommerce/auth/infrastructure/security/JwtTokenProvider.java:78-88`

**Current Code:**
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()...parseSignedClaims(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

**Problem:**
- Catching generic Exception loses information about why token is invalid
- Can't distinguish between expired, malformed, or signature mismatch

**Recommendation:**
```java
public TokenValidationResult validateToken(String token) {
    try {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        return TokenValidationResult.valid();
    } catch (ExpiredJwtException e) {
        return TokenValidationResult.expired();
    } catch (MalformedJwtException e) {
        return TokenValidationResult.malformed();
    } catch (SignatureException e) {
        return TokenValidationResult.invalidSignature();
    }
}
```

**Status:** [ ] Not Started

---

### CQ-004 :yellow_circle: Hardcoded Values

**File:** `common-lib/src/main/java/com/ecommerce/common/outbox/OutboxPoller.java:97`

**Current Code:**
```java
int daysToKeep = 7;
```

**Problem:**
- `daysToKeep = 7` is hardcoded while `batchSize` is externalized
- Inconsistent approach

**Recommendation:**
```java
@Value("${outbox.cleanup.days-to-keep:7}")
private int daysToKeep;
```

**Status:** [ ] Not Started

---

### CQ-005 :yellow_circle: SuppressWarnings Abuse

**Files:** Multiple locations

**Current Code:**
```java
@SuppressWarnings("null")
private boolean isAlreadyProcessed(String eventId) {
    UUID uuid = UUID.fromString(eventId);
```

**Problem:**
- `@SuppressWarnings("null")` used to suppress warnings instead of properly handling null cases

**Recommendation:**
Handle null properly:
```java
private boolean isAlreadyProcessed(String eventId) {
    if (eventId == null || eventId.isBlank()) {
        throw new IllegalArgumentException("Event ID cannot be null or blank");
    }
    UUID uuid = UUID.fromString(eventId);
    return processedEventRepository.existsById(uuid);
}
```

**Status:** [ ] Not Started

---

### CQ-006 :yellow_circle: Missing Null Checks in Domain Events

**File:** `common-lib/src/main/java/com/ecommerce/common/events/OrderEvents.java`

**Problem:**
- Record constructors don't validate null inputs
- Could lead to NullPointerException when processing events

**Recommendation:**
```java
public record OrderCreated(...) implements OrderEvents {
    public OrderCreated {
        Objects.requireNonNull(eventId, "eventId required");
        Objects.requireNonNull(orderId, "orderId required");
        // ...
    }
}
```

**Status:** [ ] Not Started

---

## 5. Saga Pattern Issues

### SAGA-001 :orange_circle: Saga State Transitions Not Enforced

**File:** `order-service/src/main/java/com/ecommerce/order/domain/saga/OrderSaga.java:73-79`

**Current Code:**
```java
public void startCompensating() {
    if (state != SagaState.PAYMENT_FAILED && state != SagaState.INVENTORY_FAILED) {
        // We might also allow compensating from other states if needed
    }
    this.state = SagaState.COMPENSATING;
    this.updatedAt = Instant.now();
}
```

**Problems:**
1. Empty if-block doesn't enforce state transition rules
2. Comment "might also allow" shows design is not finalized
3. Can transition to COMPENSATING from any state

**Recommendation:**
```java
public void startCompensating() {
    if (state != SagaState.PAYMENT_FAILED && state != SagaState.INVENTORY_FAILED) {
        throw new IllegalStateException(
            "Cannot start compensating from state: " + state);
    }
    this.state = SagaState.COMPENSATING;
    this.updatedAt = Instant.now();
}
```

**Status:** [ ] Not Started

---

### SAGA-002 :yellow_circle: Missing Saga Timeout Handling

**Problem:**
- No mechanism to handle saga stuck in intermediate state
- If a service is down, saga could be stuck forever at `STARTED` or `INVENTORY_RESERVED`

**Recommendation:**
Implement scheduled job to detect and handle stuck sagas:
```java
@Scheduled(fixedDelay = 60000)
public void handleStuckSagas() {
    List<OrderSaga> stuckSagas = sagaRepository.findByStateInAndUpdatedAtBefore(
        List.of(SagaState.STARTED, SagaState.INVENTORY_RESERVED),
        Instant.now().minus(5, ChronoUnit.MINUTES)
    );

    for (OrderSaga saga : stuckSagas) {
        log.warn("Found stuck saga: {}, state: {}", saga.getId(), saga.getState());
        // Compensate or retry based on state
    }
}
```

**Status:** [ ] Not Started

---

## 6. Domain Model Issues

### DM-001 :yellow_circle: Inconsistent Domain Event Handling

**File:** `order-service/src/main/java/com/ecommerce/order/domain/model/Order.java:72-79`

**Problem:**
- Domain events registered in aggregate but may never be published if aggregate is not persisted correctly
- No mechanism to ensure events are cleared after persistence

**Recommendation:**
Add event clearing mechanism:
```java
// In persistence adapter after save
order.clearDomainEvents();

// Or publish events explicitly
List<DomainEvent> events = order.getDomainEvents();
order.clearDomainEvents();
events.forEach(eventPublisher::publish);
```

**Status:** [ ] Not Started

---

### DM-002 :yellow_circle: User Domain Model Lacks Validation

**File:** `auth-service/src/main/java/com/ecommerce/auth/domain/model/User.java:28-36`

**Current Code:**
```java
public User(UserId id, String username, String email, String passwordHash, Set<Role> roles) {
    this.id = id;
    this.username = username;
    this.email = email;
    // ...
}
```

**Problem:**
- Domain model doesn't validate business rules (username length, email format)
- DDD principle: Domain model should be always-valid

**Recommendation:**
```java
public User(UserId id, String username, String email, String passwordHash, Set<Role> roles) {
    Objects.requireNonNull(id, "User ID required");
    validateUsername(username);
    validateEmail(email);
    Objects.requireNonNull(passwordHash, "Password hash required");

    this.id = id;
    this.username = username;
    // ...
}

private void validateUsername(String username) {
    if (username == null || username.length() < 3 || username.length() > 30) {
        throw new IllegalArgumentException("Username must be 3-30 characters");
    }
    if (!username.matches("^[a-zA-Z0-9_]+$")) {
        throw new IllegalArgumentException("Username must be alphanumeric");
    }
}
```

**Status:** [ ] Not Started

---

## 7. Observability Issues

### OBS-001 :yellow_circle: Missing Trace Context in Kafka Events

**Problem:**
- Trace context (`traceId`, `spanId`) not propagated through Kafka messages
- Distributed tracing broken when debugging async flows

**Recommendation:**
Include trace headers in Kafka message:
```java
// Producer side
ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
record.headers().add("traceId", tracer.currentSpan().context().traceId().getBytes());

// Consumer side
String traceId = new String(record.headers().lastHeader("traceId").value());
```

Or use Spring Kafka's built-in tracing propagation.

**Status:** [ ] Not Started

---

### OBS-002 :yellow_circle: Metric Cardinality Risk

**File:** `inventory-service/src/main/java/com/ecommerce/inventory/application/service/InventoryService.java:86-88`

**Current Code:**
```java
meterRegistry.counter("inventory_reservation_total",
        "product", command.productId(),
        "status", "success").increment();
```

**Problem:**
- Using `productId` as metric tag can lead to high cardinality with many products
- Prometheus will have performance issues

**Recommendation:**
Remove high-cardinality tags or use bounded cardinality:
```java
// Option 1: Remove productId tag
meterRegistry.counter("inventory_reservation_total", "status", "success").increment();

// Option 2: Use product category instead
meterRegistry.counter("inventory_reservation_total",
    "category", getProductCategory(command.productId()),
    "status", "success").increment();
```

**Status:** [ ] Not Started

---

## Priority Fix Order

### P0 - Must Fix Before Production
1. [SEC-001] JWT Secret Key Validation
2. [SEC-002] HTTP Header Injection
3. [SEC-005] Timing Attack in Login

### P1 - Should Fix Soon
4. [CONC-001] Race Condition in Idempotency
5. [ARCH-003] Incomplete Saga Compensation
6. [ARCH-002] Transaction Boundary in PaymentService
7. [CQ-001] Swallowed Exceptions

### P2 - Fix When Possible
8. [ARCH-001] DDD Violation in Kafka Consumer
9. [CQ-002] Brittle Event Type Detection
10. [SAGA-001] Saga State Transitions
11. [SEC-003] Input Validation for Registration
12. [SEC-004] NPE in Rate Limiting

### P3 - Nice to Have
13. Remaining Minor issues

---

## Changelog

| Date | Author | Changes |
|------|--------|---------|
| 2026-01-18 | Senior Reviewer | Initial review completed |

---

## Sign-off

- [ ] Development Lead Review
- [ ] Security Review
- [ ] Architecture Review
- [ ] All Critical Issues Resolved
- [ ] All Major Issues Resolved or Accepted
