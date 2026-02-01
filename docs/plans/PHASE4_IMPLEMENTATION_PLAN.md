# 🛡️ Phase 4: Resilience & Testing - Implementation Plan

## Overview

**Objective**: Thêm Resilience Patterns (Circuit Breaker, Retry, Bulkhead), Chaos Engineering, và Comprehensive Testing để đảm bảo hệ thống **fault-tolerant** và **self-healing**.

**Nguyên tắc thiết kế**:
- ✅ **Fail fast, recover faster** - Detect failures sớm, isolate và recover tự động
- ✅ **Test in production-like environment** - Chaos testing trước khi lên production
- ✅ **Contract-first** - API contracts được verify trong CI/CD
- ✅ **Measure everything** - Metrics cho mọi resilience pattern
- ❌ **Avoid over-engineering** - Chỉ implement patterns cần thiết

**Phase 3 Foundation**:
- ✅ 7 services: api-gateway (8080), auth (8086), product (8081), inventory (8082), order (8083), payment (8084), notification (8085)
- ✅ JWT Authentication + Rate Limiting
- ✅ Distributed Tracing (Micrometer + Zipkin)
- ✅ Prometheus + Grafana Observability
- ✅ Saga Pattern + Transactional Outbox
- ✅ Idempotency (API + Kafka consumers)

---

## 🔍 Current State Analysis

### Đã có sẵn (Resilience)

| Pattern | Implementation | Location |
|---------|----------------|----------|
| Timeout | 3s fixed timeout | `ProductServiceClient`, `InventoryServiceClient` |
| Idempotency | Database-backed | `OrderService`, `IdempotentEventHandler` |
| Saga Pattern | Hybrid choreography | `OrderSaga` |
| Transactional Outbox | Scheduled polling | `OutboxPoller` |
| Rate Limiting | Redis-backed | `RateLimitConfig` (API Gateway) |

### Gap Analysis

| Pattern | Status | Impact |
|---------|--------|--------|
| Circuit Breaker | ❌ Missing | Cascade failures possible |
| Retry (Exponential Backoff) | ❌ Missing | Transient failures cause immediate errors |
| Bulkhead | ❌ Missing | One slow service affects all |
| Fallback Mechanisms | ❌ Missing | No graceful degradation |
| Chaos Testing | ❌ Missing | Unknown failure modes |
| Contract Testing | ❌ Missing | Breaking changes detected in production |
| Load Testing | ❌ Missing | Performance baseline unknown |
| E2E Testing | ❌ Missing | Business flows not validated end-to-end |

---

## 🏗️ Target Architecture

```
                                    ┌─────────────────┐
                                    │   Prometheus    │
                                    │   + Grafana     │
                                    │  (Resilience    │
                                    │   Dashboards)   │
                                    └────────▲────────┘
                                             │ scrape metrics
┌──────────┐                    ┌────────────┴───────────────┐
│  Client  │───────────────────►│       API Gateway          │
└──────────┘                    │  + Rate Limiting           │
                                └─────────────┬──────────────┘
                                              │
                    ┌─────────────────────────┼─────────────────────────┐
                    │                         │                         │
                    ▼                         ▼                         ▼
          ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
          │  Order Service  │       │ Product Service │       │Inventory Service│
          │                 │       │                 │       │                 │
          │ ┌─────────────┐ │       │                 │       │                 │
          │ │CircuitBreaker│ │◄─────►│                 │◄─────►│                 │
          │ │   + Retry   │ │       │                 │       │                 │
          │ │  + Bulkhead │ │       │                 │       │                 │
          │ │  + Fallback │ │       │                 │       │                 │
          │ └─────────────┘ │       │                 │       │                 │
          └────────┬────────┘       └─────────────────┘       └─────────────────┘
                   │
                   │ Kafka (Outbox)
                   ▼
          ┌─────────────────┐       ┌─────────────────┐
          │ Payment Service │       │  Notification   │
          │                 │       │    Service      │
          └─────────────────┘       └─────────────────┘
```

---

## 📋 Phase Overview

| Sub-Phase | Focus | Priority | Duration |
|-----------|-------|----------|----------|
| **4.1** | Resilience4j Integration | 🔴 Critical | Week 1-2 |
| **4.2** | Fallback & Graceful Degradation | 🔴 Critical | Week 3 |
| **4.3** | Chaos Engineering | 🟡 High | Week 4 |
| **4.4** | Contract Testing | 🟡 High | Week 5 |
| **4.5** | Load & Performance Testing | 🟢 Medium | Week 6 |
| **4.6** | E2E Testing & Test Orchestration | 🟢 Medium | Week 7 |

---

## 📦 Phase 4.1: Resilience4j Integration

### Mục tiêu

Thêm **Circuit Breaker**, **Retry**, **Bulkhead**, và **TimeLimiter** cho tất cả synchronous inter-service calls, giúp hệ thống tự bảo vệ khi downstream service gặp sự cố.

### Output mong muốn

```
common-lib/
├── src/main/java/com/ecommerce/common/resilience/
│   ├── ResilienceConfig.java          # Default configurations
│   ├── CircuitBreakerRegistry.java    # Centralized registry
│   └── FallbackFactory.java           # Generic fallback factory

order-service/
├── src/main/java/.../infrastructure/client/
│   ├── ProductServiceClient.java       # + @CircuitBreaker, @Retry
│   ├── InventoryServiceClient.java     # + @CircuitBreaker, @Retry
│   └── fallback/
│       ├── ProductServiceFallback.java
│       └── InventoryServiceFallback.java
├── src/main/resources/
│   └── application.yml                  # Resilience4j configs
```

### Technical Design

#### Circuit Breaker State Machine

```
     ┌─────────────────────────────────────────┐
     │                                         │
     ▼                                         │
  CLOSED ──────────► OPEN ──────────► HALF_OPEN
     ▲                                    │
     │                                    │
     └────────────────────────────────────┘
           success threshold met
```

#### Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 30s
        failureRateThreshold: 50
        slowCallRateThreshold: 80
        slowCallDurationThreshold: 2s
        recordExceptions:
          - java.net.ConnectException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.reactive.function.client.WebClientRequestException
        ignoreExceptions:
          - com.ecommerce.common.exception.BusinessException
    instances:
      product-service:
        baseConfig: default
      inventory-service:
        baseConfig: default
        failureRateThreshold: 40  # More sensitive

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.ConnectException
          - java.util.concurrent.TimeoutException
        ignoreExceptions:
          - com.ecommerce.common.exception.BusinessException
    instances:
      product-service:
        baseConfig: default
      inventory-service:
        baseConfig: default
        maxAttempts: 4

  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 25
        maxWaitDuration: 500ms
    instances:
      product-service:
        baseConfig: default
      inventory-service:
        maxConcurrentCalls: 50
        maxWaitDuration: 1s

  timelimiter:
    configs:
      default:
        timeoutDuration: 3s
        cancelRunningFuture: true
    instances:
      product-service:
        baseConfig: default
      inventory-service:
        timeoutDuration: 5s
```

#### Implementation Pattern

```java
@Service
@RequiredArgsConstructor
public class ProductServiceClient implements ProductServicePort {

    private final WebClient webClient;
    private final RedisTemplate<String, ProductDTO> cache;

    @Override
    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductFallback")
    @Retry(name = "product-service")
    @Bulkhead(name = "product-service")
    @TimeLimiter(name = "product-service")
    public CompletableFuture<ProductDTO> getProduct(String productId) {
        return webClient.get()
            .uri("/api/products/{id}", productId)
            .retrieve()
            .bodyToMono(ProductDTO.class)
            .toFuture();
    }

    private CompletableFuture<ProductDTO> getProductFallback(String productId, Exception ex) {
        // Try cache first
        var cached = cache.opsForValue().get("product:" + productId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.withDegradedFlag(true));
        }
        // Return placeholder
        return CompletableFuture.completedFuture(ProductDTO.placeholder(productId));
    }
}
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | CircuitBreaker hoạt động cho ProductServiceClient | Unit test: mock 5 failures → Circuit OPEN |
| 2 | CircuitBreaker hoạt động cho InventoryServiceClient | Unit test: tương tự |
| 3 | Retry với exponential backoff | Unit test: verify 3 attempts với delays 500ms, 1s, 2s |
| 4 | Bulkhead isolate concurrent calls | Load test: 100 concurrent → max 25 tới product-service |
| 5 | TimeLimiter timeout thay thế manual | Verify old timeout code removed |
| 6 | Metrics exposed to Prometheus | `/actuator/prometheus` có `resilience4j_*` metrics |
| 7 | Grafana dashboard hiển thị circuit states | Dashboard panel cho circuit breaker status |
| 8 | Không breaking change API | Existing tests vẫn pass |

---

## 📦 Phase 4.2: Fallback & Graceful Degradation

### Mục tiêu

Khi downstream service unavailable, hệ thống **không crash** mà trả về **degraded response** hợp lý, đảm bảo core business flow vẫn hoạt động ở mức tối thiểu.

### Output mong muốn

```
order-service/
├── src/main/java/.../infrastructure/client/fallback/
│   ├── ProductServiceFallback.java
│   │   └── getCachedProduct()        # Return từ Redis cache
│   │   └── getDefaultProduct()       # Return generic product info
│   └── InventoryServiceFallback.java
│       └── reserveWithRetry()        # Queue for later processing
│       └── getStockEstimate()        # Return last known stock

├── src/main/java/.../domain/
│   └── RetryTask.java                # Retry queue entity
│   └── RetryTaskRepository.java

├── src/main/java/.../application/service/
│   └── RetryTaskProcessor.java       # Background retry worker

common-lib/
├── src/main/java/.../resilience/
│   └── DegradedResponse.java         # Wrapper with degraded flag
│   └── DegradedResponseFactory.java  # Standardized degraded responses
```

### Fallback Strategy Matrix

| Service Call | Primary Action | Fallback Action | User Impact |
|--------------|----------------|-----------------|-------------|
| `getProduct()` | HTTP call | Redis cache | None (if cache fresh) |
| `getProduct()` (cache miss) | HTTP call | Placeholder DTO | "Details loading..." |
| `reserveStock()` | HTTP call | Queue retry task | Order: PENDING_INVENTORY |
| `checkStock()` | HTTP call | Return UNKNOWN | "Stock TBD" |
| `processPayment()` | HTTP call | Queue retry task | Order: PENDING_PAYMENT |

### Technical Design

#### Retry Queue Entity

```java
@Entity
@Table(name = "retry_tasks")
public record RetryTaskEntity(
    @Id UUID id,
    String taskType,           // RESERVE_STOCK, PROCESS_PAYMENT
    String payload,            // JSON serialized command
    int attemptCount,
    int maxAttempts,
    Instant nextRetryAt,
    Instant createdAt,
    RetryStatus status         // PENDING, PROCESSING, COMPLETED, FAILED
) {}
```

#### Retry Task Processor

```java
@Service
@RequiredArgsConstructor
public class RetryTaskProcessor {

    private final RetryTaskRepository repository;
    private final InventoryServicePort inventoryService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingTasks() {
        var tasks = repository.findPendingTasks(Instant.now(), PageRequest.of(0, 10));

        for (var task : tasks) {
            try {
                processTask(task);
                repository.markCompleted(task.id());
            } catch (Exception ex) {
                handleRetryFailure(task, ex);
            }
        }
    }

    private void handleRetryFailure(RetryTaskEntity task, Exception ex) {
        if (task.attemptCount() >= task.maxAttempts()) {
            repository.markFailed(task.id());
            // Trigger alert/notification
        } else {
            var nextRetry = calculateNextRetry(task.attemptCount());
            repository.scheduleRetry(task.id(), nextRetry);
        }
    }
}
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | Product cache fallback hoạt động | Test: stop product-service → order still created with cached data |
| 2 | Inventory queue fallback | Test: stop inventory → order status = PENDING_INVENTORY |
| 3 | Retry queue worker processes tasks | Test: restart inventory → queued reservations processed |
| 4 | Degraded response có flag rõ ràng | API response có `"degraded": true` |
| 5 | Retry task có exponential backoff | Verify retry delays: 5s, 10s, 20s, 40s, 80s |
| 6 | Failed tasks trigger alerts | Alert sent when task.attemptCount >= maxAttempts |
| 7 | Metrics cho fallback calls | Prometheus có `fallback_invocations_total` |

---

## 📦 Phase 4.3: Chaos Engineering

### Mục tiêu

Proactively inject failures vào production-like environment để **validate** resilience patterns hoạt động đúng, và phát hiện **unknown failure modes**.

### Output mong muốn

```
chaos-testing/
├── build.gradle.kts
├── src/main/java/com/ecommerce/chaos/
│   ├── ChaosMonkeyConfig.java
│   └── scenarios/
│       ├── LatencyAttack.java
│       ├── ExceptionAttack.java
│       └── KillServiceAttack.java
├── src/test/java/.../
│   └── ChaosScenarioTest.java
└── scenarios/
    ├── inventory-latency.yaml
    ├── payment-failure.yaml
    └── kafka-partition-loss.yaml

docker/
├── docker-compose.chaos.yml           # Chaos testing environment
└── toxiproxy/
    └── toxiproxy.json                  # Proxy configurations
```

### Chaos Scenarios

| Scenario | Target | Attack Type | Expected Behavior |
|----------|--------|-------------|-------------------|
| **Inventory Latency** | inventory-service | 5s latency | Circuit opens → fallback queue |
| **Payment Exception** | payment-service | 50% exceptions | Saga compensates → order cancelled |
| **Product Kill** | product-service | Container stop | Cache fallback → degraded response |
| **Kafka Lag** | Kafka | Pause consumer | Outbox retries → no data loss |
| **Database Slow** | PostgreSQL | 3s query delay | Timeouts → alerts |
| **Memory Pressure** | order-service | 90% memory fill | GC spikes → no OOM |
| **Network Partition** | Between services | Drop packets | Retries → eventual success |

### Tools Selection

| Tool | Use Case | Environment |
|------|----------|-------------|
| **Chaos Monkey for Spring Boot** | Application-level chaos | Dev/Staging |
| **Toxiproxy** | Network chaos (latency, packet loss) | Docker Compose |
| **Chaos Mesh** | Kubernetes-native chaos | K8s (future) |

### Technical Design

#### Chaos Monkey Configuration

```yaml
# application-chaos.yml
chaos:
  monkey:
    enabled: true
    assaults:
      level: 5                    # 1-10, higher = more chaos
      latencyRangeStart: 1000
      latencyRangeEnd: 5000
      latencyActive: true
      exceptionsActive: true
      killApplicationActive: false
      memoryActive: false
    watcher:
      component: true
      service: true
      repository: true
      restController: true
```

#### Toxiproxy Docker Setup

```yaml
# docker-compose.chaos.yml
services:
  toxiproxy:
    image: ghcr.io/shopify/toxiproxy:latest
    ports:
      - "8474:8474"    # API port
      - "5433:5433"    # Proxied PostgreSQL
      - "6380:6380"    # Proxied Redis
    volumes:
      - ./toxiproxy/toxiproxy.json:/config/toxiproxy.json
    command: ["-config", "/config/toxiproxy.json"]

  order-service:
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://toxiproxy:5433/orders
      SPRING_REDIS_HOST: toxiproxy
      SPRING_REDIS_PORT: 6380
```

#### Chaos Test Example

```java
@SpringBootTest
@ActiveProfiles("chaos")
class ChaosScenarioTest {

    @Autowired
    private ChaosMonkeySettings chaosMonkeySettings;

    @Autowired
    private OrderService orderService;

    @Test
    void circuitBreaker_shouldOpen_whenInventoryLatencyHigh() {
        // Given: Enable latency assault
        chaosMonkeySettings.getAssaultProperties().setLatencyActive(true);
        chaosMonkeySettings.getAssaultProperties().setLatencyRangeStart(5000);
        chaosMonkeySettings.getAssaultProperties().setLatencyRangeEnd(5000);

        // When: Make multiple requests
        var results = IntStream.range(0, 10)
            .mapToObj(i -> orderService.createOrder(testCommand()))
            .toList();

        // Then: Circuit should be open, fallback used
        var circuitBreaker = circuitBreakerRegistry.circuitBreaker("inventory-service");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // And: Orders should be in PENDING_INVENTORY state
        var pendingOrders = results.stream()
            .filter(r -> r.status() == OrderStatus.PENDING_INVENTORY)
            .count();
        assertThat(pendingOrders).isGreaterThan(5);
    }
}
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | Chaos Monkey integrated | Enable chaos → exceptions injected |
| 2 | Toxiproxy configured | Latency injection works via API |
| 3 | Latency attack triggers circuit | 5s latency → circuit OPEN trong 30s |
| 4 | Exception attack triggers retry | 50% exceptions → success after retries |
| 5 | Service kill triggers fallback | Kill product-service → cache used |
| 6 | No data loss during Kafka chaos | Pause consumer → resume → all processed |
| 7 | Chaos test suite passes | `./gradlew :chaos-testing:test` green |
| 8 | Runbooks documented | docs/runbooks/chaos-scenarios.md created |

---

## 📦 Phase 4.4: Contract Testing

### Mục tiêu

Đảm bảo **API contracts** giữa services không bị break khi có changes, detect breaking changes **sớm** trong CI pipeline.

### Output mong muốn

```
contracts/
├── product-service/
│   └── rest/
│       ├── getProduct.groovy
│       ├── listProducts.groovy
│       └── getProductNotFound.groovy
├── inventory-service/
│   └── rest/
│       ├── checkStock.groovy
│       ├── reserveStock.groovy
│       └── releaseStock.groovy
└── order-service/
    └── messaging/
        ├── orderCreated.groovy
        ├── orderConfirmed.groovy
        └── orderCancelled.groovy

product-service/
├── src/test/java/.../contract/
│   └── ProductContractVerifierTest.java
└── build.gradle.kts                        # + contract verifier

order-service/
├── src/test/java/.../contract/
│   ├── ProductClientContractTest.java      # Consumer test
│   └── InventoryClientContractTest.java
└── build.gradle.kts                        # + stub runner
```

### Contract Testing Flow

```
┌─────────────────┐         ┌─────────────────┐
│  Order Service  │         │ Product Service │
│   (Consumer)    │         │   (Producer)    │
└────────┬────────┘         └────────┬────────┘
         │                           │
         │  1. Define Contract       │
         │  ──────────────────────►  │
         │                           │
         │  2. Generate Stub         │
         │  ◄──────────────────────  │
         │                           │
         │  3. Consumer tests        │  4. Producer tests
         │     against stub          │     against contract
         │                           │
         ▼                           ▼
   ✅ Consumer verified         ✅ Producer verified
```

### Technical Design

#### REST Contract Example

```groovy
// contracts/product-service/rest/getProduct.groovy
package contracts.rest

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return product by ID"

    request {
        method GET()
        url "/api/products/prod-123"
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: "prod-123",
            name: $(regex('.+')),
            description: $(optional(regex('.*'))),
            price: $(regex('[0-9]+\\.?[0-9]*')),
            currency: "VND",
            status: $(anyOf('ACTIVE', 'INACTIVE', 'DISCONTINUED'))
        ])
    }
}
```

#### Messaging Contract Example

```groovy
// contracts/order-service/messaging/orderCreated.groovy
package contracts.messaging

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "OrderCreated event published when order is created"

    label "orderCreated"

    input {
        triggeredBy('createOrder()')
    }

    outputMessage {
        sentTo "order-events"
        headers {
            header("eventType", "OrderCreated")
            header("correlationId", $(regex('[a-f0-9-]+')))
        }
        body([
            eventId: $(regex('[a-f0-9-]+')),
            eventType: "OrderCreated",
            timestamp: $(iso8601WithOffset()),
            payload: [
                orderId: $(regex('[a-f0-9-]+')),
                customerId: $(regex('[a-f0-9-]+')),
                items: $(regex('\\[.*\\]')),
                totalAmount: $(regex('[0-9]+\\.?[0-9]*')),
                currency: "VND"
            ]
        ])
    }
}
```

#### Producer Test (Auto-generated)

```java
// Generated by Spring Cloud Contract Verifier
public class ProductContractVerifierTest extends ContractVerifierBase {

    @Test
    public void validate_getProduct() throws Exception {
        // Given
        MockMvcRequestSpecification request = given()
            .header("Accept", "application/json");

        // When
        ResponseOptions response = given().spec(request)
            .get("/api/products/prod-123");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.header("Content-Type")).contains("application/json");

        DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
        assertThatJson(parsedJson).field("['id']").isEqualTo("prod-123");
        assertThatJson(parsedJson).field("['name']").matches(".+");
        assertThatJson(parsedJson).field("['currency']").isEqualTo("VND");
    }
}
```

#### Consumer Test (Stub Runner)

```java
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.ecommerce:product-service:+:stubs:8081",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class ProductClientContractTest {

    @Autowired
    private ProductServiceClient productClient;

    @Test
    void shouldGetProductFromStub() {
        // When
        var product = productClient.getProduct("prod-123").block();

        // Then
        assertThat(product.id()).isEqualTo("prod-123");
        assertThat(product.currency()).isEqualTo("VND");
    }
}
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | Product Service contracts defined | 3+ REST contracts in contracts/product-service/ |
| 2 | Inventory Service contracts defined | 3+ REST contracts in contracts/inventory-service/ |
| 3 | Order Service messaging contracts | 3+ Kafka contracts in contracts/order-service/ |
| 4 | Producer tests pass | `./gradlew :product-service:contractTest` green |
| 5 | Consumer tests pass | `./gradlew :order-service:contractTest` green |
| 6 | Stubs published | Stubs available in local Maven repo |
| 7 | CI pipeline runs contracts | GitHub Actions job for contract verification |
| 8 | Breaking change detected | PR with breaking change → build fails |

---

## 📦 Phase 4.5: Load & Performance Testing

### Mục tiêu

Xác định **performance baseline**, tìm **bottlenecks**, và validate hệ thống handle được **expected load**.

### Output mong muốn

```
load-testing/
├── build.gradle.kts
├── src/gatling/scala/com/ecommerce/
│   ├── simulations/
│   │   ├── OrderCreationSimulation.scala
│   │   ├── ProductBrowsingSimulation.scala
│   │   ├── CheckoutFlowSimulation.scala
│   │   └── MixedWorkloadSimulation.scala
│   └── scenarios/
│       ├── BaselineScenario.scala
│       ├── RampUpScenario.scala
│       ├── SpikeScenario.scala
│       └── SoakScenario.scala
├── src/gatling/resources/
│   ├── bodies/
│   │   ├── createOrder.json
│   │   └── createProduct.json
│   └── conf/
│       └── gatling.conf
└── results/
    └── .gitkeep

docs/performance/
├── BASELINE_METRICS.md
├── CAPACITY_PLANNING.md
└── PERFORMANCE_RUNBOOK.md
```

### Performance Targets

| Metric | Target | Critical Threshold | Action if Exceeded |
|--------|--------|-------------------|-------------------|
| P50 Latency (order) | < 200ms | < 500ms | Optimize slow queries |
| P99 Latency (order) | < 1s | < 3s | Add caching, optimize |
| P50 Latency (browse) | < 50ms | < 150ms | Check cache hit rate |
| Throughput | > 100 orders/sec | > 50 orders/sec | Scale horizontally |
| Error Rate | < 0.1% | < 1% | Check circuit breakers |
| CPU Usage | < 70% | < 90% | Scale up/out |
| Memory Usage | < 80% | < 95% | Check for leaks |

### Load Test Scenarios

| Scenario | User Pattern | Duration | Purpose |
|----------|--------------|----------|---------|
| **Baseline** | Constant 50 users | 10 min | Establish baseline metrics |
| **Ramp Up** | 0 → 500 users | 20 min | Find breaking point |
| **Spike** | 50 → 300 → 50 | 10 min | Test auto-recovery |
| **Soak** | Constant 100 users | 2 hours | Memory leaks, connection leaks |
| **Stress** | 500+ users | 30 min | System limits |

### Technical Design

#### Gatling Simulation

```scala
package com.ecommerce.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class OrderCreationSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("Authorization", "Bearer ${authToken}")

  val authenticate = exec(
    http("Login")
      .post("/auth/login")
      .body(StringBody("""{"username":"test@example.com","password":"password"}"""))
      .check(jsonPath("$.accessToken").saveAs("authToken"))
  )

  val browseProducts = exec(
    http("List Products")
      .get("/api/products")
      .check(status.is(200))
      .check(jsonPath("$[0].id").saveAs("productId"))
  )

  val createOrder = exec(
    http("Create Order")
      .post("/api/orders")
      .body(ElFileBody("bodies/createOrder.json"))
      .check(status.is(202))
      .check(jsonPath("$.orderId").saveAs("orderId"))
  )

  val checkOrderStatus = exec(
    http("Get Order")
      .get("/api/orders/${orderId}")
      .check(status.is(200))
  )

  val orderFlow = scenario("Order Creation Flow")
    .exec(authenticate)
    .pause(1)
    .exec(browseProducts)
    .pause(500.milliseconds)
    .exec(createOrder)
    .pause(2)
    .exec(checkOrderStatus)

  setUp(
    orderFlow.inject(
      rampUsers(100).during(5.minutes),
      constantUsersPerSec(20).during(10.minutes)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile(50).lt(200),
     global.responseTime.percentile(99).lt(1000),
     global.successfulRequests.percent.gt(99)
   )
}
```

#### Baseline Metrics Template

```markdown
# Performance Baseline - Order Service

## Test Environment
- Date: YYYY-MM-DD
- Environment: Staging
- Infrastructure: 2 CPU, 4GB RAM per service
- Database: PostgreSQL 16 (2 CPU, 8GB RAM)

## Results

| Metric | Value | Target | Status |
|--------|-------|--------|--------|
| P50 Response Time | XXX ms | < 200ms | ✅/❌ |
| P99 Response Time | XXX ms | < 1000ms | ✅/❌ |
| Throughput | XXX req/sec | > 100 | ✅/❌ |
| Error Rate | X.XX% | < 0.1% | ✅/❌ |
| Max Concurrent Users | XXX | > 200 | ✅/❌ |

## Bottlenecks Identified
1. ...
2. ...

## Recommendations
1. ...
2. ...
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | Gatling module setup | `./gradlew :load-testing:gatlingRun` works |
| 2 | Baseline test executed | Results documented in BASELINE_METRICS.md |
| 3 | Order creation P99 < 1s | Gatling assertion passes |
| 4 | Product browsing P99 < 150ms | Gatling assertion passes |
| 5 | Soak test passes | No memory leaks after 2 hours |
| 6 | Breaking point identified | Document max concurrent users |
| 7 | CI weekly perf tests | Scheduled GitHub Actions job |
| 8 | Performance alerts | Alert when P99 increases > 20% |

---

## 📦 Phase 4.6: E2E Testing & Test Orchestration

### Mục tiêu

**End-to-end tests** validate toàn bộ business flows, và **test orchestration** đảm bảo tests chạy reliable trong CI/CD.

### Output mong muốn

```
e2e-testing/
├── build.gradle.kts
├── src/test/java/com/ecommerce/e2e/
│   ├── config/
│   │   ├── TestEnvironmentConfig.java
│   │   └── TestContainersConfig.java
│   ├── flows/
│   │   ├── OrderHappyPathE2ETest.java
│   │   ├── OrderOutOfStockE2ETest.java
│   │   ├── OrderPaymentFailedE2ETest.java
│   │   └── ConcurrentOrdersE2ETest.java
│   ├── fixtures/
│   │   ├── TestDataFactory.java
│   │   └── TestUserFactory.java
│   └── utils/
│       └── EventuallyAssertions.java
├── docker/
│   └── docker-compose.e2e.yml
└── src/test/resources/
    └── application-e2e.yml

.github/workflows/
├── ci.yml                      # Main CI pipeline
├── e2e-tests.yml              # E2E workflow
├── contract-tests.yml         # Contract verification
└── performance-tests.yml      # Weekly perf tests
```

### E2E Test Scenarios

| Flow | Steps | Assertions |
|------|-------|------------|
| **Happy Path** | Login → Browse → Order → Pay | Order CONFIRMED, Stock reduced |
| **Out of Stock** | Order exceeds stock | Order CANCELLED, User notified |
| **Payment Failed** | Order → Payment declined | Stock released, Order cancelled |
| **Concurrent Orders** | 10 orders, 1 item left | 1 success, 9 out-of-stock |
| **Service Recovery** | Order → Kill service → Restart | Order eventually completes |

### Technical Design

#### TestContainers Setup

```java
@TestConfiguration
public class TestContainersConfig {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("ecommerce_test");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);
}
```

#### E2E Test Example

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
class OrderHappyPathE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCompleteFullOrderFlow() {
        // Given: Product with stock
        var product = productRepository.save(TestDataFactory.product(stock = 10));
        var customer = TestUserFactory.customer();
        var token = authenticate(customer);

        // When: Place order
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(
            new CreateOrderRequest(List.of(
                new OrderItem(product.getId(), 2)
            )),
            headers
        );

        var response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);

        // Then: Order accepted
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        var orderId = response.getBody().orderId();

        // And: Order confirmed eventually
        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                var order = orderRepository.findById(orderId).orElseThrow();
                assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            });

        // And: Stock reduced
        var updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(8);
    }
}
```

#### CI Pipeline Orchestration

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run unit tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v4

  integration-tests:
    needs: unit-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run integration tests
        run: ./gradlew integrationTest

  contract-tests:
    needs: unit-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run contract tests
        run: ./gradlew contractTest
      - name: Publish stubs
        if: github.ref == 'refs/heads/main'
        run: ./gradlew publishStubsPublicationToMavenLocal

  e2e-tests:
    needs: [integration-tests, contract-tests]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run E2E tests
        run: ./gradlew :e2e-testing:test

  build:
    needs: e2e-tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build artifacts
        run: ./gradlew build -x test
      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: jars
          path: "**/build/libs/*.jar"
```

### Tiêu chí DONE

| # | Criteria | Verification Method |
|---|----------|---------------------|
| 1 | E2E module with TestContainers | Module compiles, containers start |
| 2 | Happy path test passes | Order created → confirmed |
| 3 | Out-of-stock test passes | Order cancelled correctly |
| 4 | Payment failed test passes | Stock released |
| 5 | Concurrent orders test | Race condition handled |
| 6 | CI pipeline orchestrates all tests | Unit → Integration → Contract → E2E |
| 7 | Test reports aggregated | HTML report with all results |
| 8 | Flaky test detection | Tests marked if fail > 2x/week |

---

## 📦 Dependencies to Add

### Root build.gradle.kts

```kotlin
plugins {
    id("io.gatling.gradle") version "3.10.5" apply false
}

ext {
    set("resilience4jVersion", "2.2.0")
    set("springCloudContractVersion", "4.1.0")
    set("testcontainersVersion", "1.19.5")
    set("awaitilityVersion", "4.2.0")
    set("chaosMonkeyVersion", "3.1.0")
}
```

### common-lib/build.gradle.kts

```kotlin
dependencies {
    // Resilience4j
    api("io.github.resilience4j:resilience4j-spring-boot3:${resilience4jVersion}")
    api("io.github.resilience4j:resilience4j-circuitbreaker:${resilience4jVersion}")
    api("io.github.resilience4j:resilience4j-retry:${resilience4jVersion}")
    api("io.github.resilience4j:resilience4j-bulkhead:${resilience4jVersion}")
    api("io.github.resilience4j:resilience4j-timelimiter:${resilience4jVersion}")
    api("io.github.resilience4j:resilience4j-micrometer:${resilience4jVersion}")
}
```

### order-service/build.gradle.kts

```kotlin
dependencies {
    // Chaos Monkey (test only)
    testImplementation("de.codecentric:chaos-monkey-spring-boot:${chaosMonkeyVersion}")

    // Contract Testing
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
}
```

### product-service/build.gradle.kts

```kotlin
plugins {
    id("org.springframework.cloud.contract") version "${springCloudContractVersion}"
}

dependencies {
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
}

contracts {
    testFramework.set(TestFramework.JUNIT5)
    contractsDslDir.set(file("${rootDir}/contracts/product-service"))
}
```

### load-testing/build.gradle.kts

```kotlin
plugins {
    id("io.gatling.gradle") version "3.10.5"
}

dependencies {
    gatling("io.gatling.highcharts:gatling-charts-highcharts:3.10.5")
}

gatling {
    logLevel = "WARN"
    simulation = "com.ecommerce.simulations.MixedWorkloadSimulation"
}
```

### e2e-testing/build.gradle.kts

```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:${testcontainersVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testcontainersVersion}")
    testImplementation("org.testcontainers:postgresql:${testcontainersVersion}")
    testImplementation("org.testcontainers:kafka:${testcontainersVersion}")
    testImplementation("org.awaitility:awaitility:${awaitilityVersion}")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
}
```

---

## 📁 Project Structure After Phase 4

```
ecommerce-backend/
├── common-lib/                     # + Resilience4j configs
├── api-gateway/                    # Port 8080
├── auth-service/                   # Port 8086
├── product-service/                # Port 8081, + contracts
├── inventory-service/              # Port 8082, + contracts
├── order-service/                  # Port 8083, + resilience, + fallbacks
├── payment-service/                # Port 8084
├── notification-service/           # Port 8085
├── chaos-testing/                  # NEW - Chaos scenarios
├── load-testing/                   # NEW - Gatling simulations
├── e2e-testing/                    # NEW - E2E tests
├── contracts/                      # NEW - API contracts
│   ├── product-service/
│   ├── inventory-service/
│   └── order-service/
├── docker/
│   ├── docker-compose.yml
│   ├── docker-compose.chaos.yml    # NEW - Chaos environment
│   ├── docker-compose.e2e.yml      # NEW - E2E environment
│   └── toxiproxy/                  # NEW - Network chaos
├── docs/
│   ├── PHASE4_IMPLEMENTATION_PLAN.md  # This file
│   ├── performance/                # NEW
│   │   ├── BASELINE_METRICS.md
│   │   └── CAPACITY_PLANNING.md
│   └── runbooks/                   # NEW
│       └── chaos-scenarios.md
└── .github/workflows/              # CI/CD pipelines
    ├── ci.yml
    ├── e2e-tests.yml
    ├── contract-tests.yml
    └── performance-tests.yml
```

---

## ✅ Phase 4 Completion Checklist

### Phase 4.1: Resilience4j ☐
- [ ] CircuitBreaker configured và hoạt động
- [ ] Retry với exponential backoff
- [ ] Bulkhead isolation
- [ ] TimeLimiter thay thế manual timeout
- [ ] Prometheus metrics exposed
- [ ] Grafana dashboard cho circuit states

### Phase 4.2: Fallback ☐
- [ ] Product cache fallback
- [ ] Inventory queue fallback
- [ ] Retry queue worker
- [ ] Degraded response flagging
- [ ] Fallback metrics và alerts

### Phase 4.3: Chaos Engineering ☐
- [ ] Chaos Monkey integrated
- [ ] Toxiproxy setup
- [ ] Latency attack scenario
- [ ] Exception attack scenario
- [ ] Service kill scenario
- [ ] Chaos test suite passes
- [ ] Runbooks documented

### Phase 4.4: Contract Testing ☐
- [ ] Spring Cloud Contract setup
- [ ] Producer contracts (product, inventory)
- [ ] Consumer tests (order)
- [ ] Messaging contracts (Kafka events)
- [ ] CI integration
- [ ] Stub publishing

### Phase 4.5: Load Testing ☐
- [ ] Gatling setup
- [ ] Baseline simulation
- [ ] Ramp-up simulation
- [ ] Spike simulation
- [ ] Soak simulation
- [ ] Baseline metrics documented
- [ ] Capacity planning document

### Phase 4.6: E2E Testing ☐
- [ ] E2E module với TestContainers
- [ ] Happy path test
- [ ] Failure scenario tests
- [ ] Concurrent order test
- [ ] CI pipeline orchestration
- [ ] Test reports aggregated

---

## 📊 Success Metrics

| KPI | Before Phase 4 | Target After | Measurement |
|-----|----------------|--------------|-------------|
| MTTR | Unknown | < 1 minute | Time from failure to recovery |
| Failure Detection | Manual | < 30 seconds | Alerting latency |
| Cascade Prevention | None | 100% | Circuit breaker effectiveness |
| Contract Violations | Production | CI/CD | Where detected |
| Perf Regression | None | Automated | Weekly checks |
| E2E Coverage | 0% | > 80% | Critical flows covered |
| Test Reliability | Unknown | < 1% flaky | Flaky test rate |

---

## ⚠️ Out of Scope (Avoid Over-engineering)

| Feature | Reason |
|---------|--------|
| Service Mesh (Istio) | Overkill cho 7 services, Resilience4j đủ dùng |
| Full Chaos Mesh | Chỉ cần khi có Kubernetes production |
| Mutation Testing | Nice-to-have, không critical |
| Visual Regression | Không có frontend trong scope |
| Mobile Testing | Không có mobile app |
| Security Penetration Testing | Separate initiative |

---

## 📚 References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract)
- [Gatling Documentation](https://gatling.io/docs/gatling/)
- [Chaos Monkey for Spring Boot](https://codecentric.github.io/chaos-monkey-spring-boot/)
- [TestContainers](https://testcontainers.com/)
- [Principles of Chaos Engineering](https://principlesofchaos.org/)
