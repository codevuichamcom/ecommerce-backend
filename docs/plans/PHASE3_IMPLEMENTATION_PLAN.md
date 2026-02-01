# 🏢 Phase 3: Enterprise Level - Implementation Plan

## Overview

**Objective**: Thêm Security, API Gateway, Caching và Observability cho hệ thống e-commerce backend.

**Nguyên tắc thiết kế**:
- ✅ **Keep it simple** - Chỉ implement những gì cần thiết
- ✅ **Leverage Spring ecosystem** - Sử dụng các tool đã có sẵn trong Spring Boot 3.x
- ✅ **Production-ready** - Có thể deploy thực tế, không chỉ là demo
- ❌ **Avoid over-engineering** - Không làm OAuth2 Authorization Server phức tạp, không service mesh

**Phase 2 Foundation**:
- ✅ 5 services: product (8081), inventory (8082), order (8083), payment (8084), notification (8085)
- ✅ Kafka event-driven communication
- ✅ Redis đã có sẵn
- ✅ Virtual Threads đã enabled

---

## 🏗️ Target Architecture

```
                                    ┌─────────────────┐
                                    │   Prometheus    │
                                    │   + Grafana     │
                                    └────────▲────────┘
                                             │ scrape metrics
┌──────────┐     HTTPS      ┌────────────────┴───────────────┐
│  Client  │ ──────────────►│         API Gateway            │
│          │◄────────────── │    (Spring Cloud Gateway)      │
└──────────┘                │  - JWT Validation              │
                            │  - Rate Limiting               │
                            │  - Request Routing             │
                            └───────────────┬────────────────┘
                                            │ HTTP (internal)
            ┌───────────────┬───────────────┼───────────────┬───────────────┐
            ▼               ▼               ▼               ▼               ▼
     ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
     │ Product  │    │Inventory │    │  Order   │    │ Payment  │    │  Notif   │
     │  8081    │    │  8082    │    │  8083    │    │  8084    │    │  8085    │
     └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
          │               │               │               │               │
          └───────────────┴───────┬───────┴───────────────┴───────────────┘
                                  │
                           ┌──────▼──────┐
                           │    Redis    │
                           │  (Cache +   │
                           │  Sessions)  │
                           └─────────────┘
```

---

## 📦 New Components

### 1. API Gateway Service (Port 8080)

```
api-gateway/
├── src/main/java/com/ecommerce/gateway/
│   ├── ApiGatewayApplication.java
│   ├── config/
│   │   ├── RouteConfig.java
│   │   ├── SecurityConfig.java
│   │   └── RateLimitConfig.java
│   └── filter/
│       ├── JwtAuthenticationFilter.java
│       └── RequestLoggingFilter.java
└── src/main/resources/
    └── application.yml
```

### 2. Auth Service (Port 8086)

**Đơn giản hóa**: Không làm full OAuth2 server. Chỉ cần:
- Login endpoint → trả về JWT
- Refresh token endpoint
- User management cơ bản

```
auth-service/
├── src/main/java/com/ecommerce/auth/
│   ├── AuthServiceApplication.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Role.java (ADMIN, CUSTOMER, SERVICE)
│   │   │   └── RefreshToken.java
│   │   └── repository/UserRepository.java
│   ├── application/
│   │   └── service/AuthService.java
│   └── infrastructure/
│       ├── security/
│       │   ├── JwtTokenProvider.java
│       │   └── PasswordEncoder.java
│       ├── persistence/
│       └── web/AuthController.java
└── src/main/resources/
    ├── application.yml
    └── db/migration/V1__create_user_tables.sql
```

---

## 🔐 Security Design (Simplified)

### JWT Flow

```
┌────────┐                  ┌─────────────┐                 ┌─────────────┐
│ Client │                  │ Auth Service│                 │ API Gateway │
└───┬────┘                  └──────┬──────┘                 └──────┬──────┘
    │                              │                               │
    │  POST /auth/login            │                               │
    │  {username, password}        │                               │
    │─────────────────────────────►│                               │
    │                              │                               │
    │  {accessToken, refreshToken} │                               │
    │◄─────────────────────────────│                               │
    │                              │                               │
    │  GET /api/orders             │                               │
    │  Authorization: Bearer xxx   │                               │
    │──────────────────────────────┼──────────────────────────────►│
    │                              │                               │
    │                              │   Validate JWT (local)        │
    │                              │   Extract user info           │
    │                              │   Forward to Order Service    │
    │                              │                               │
    │  Response                    │                               │
    │◄─────────────────────────────┼───────────────────────────────│
```

### JWT Token Structure

```json
{
  "sub": "user-123",
  "username": "john@example.com",
  "roles": ["CUSTOMER"],
  "iat": 1704067200,
  "exp": 1704070800
}
```

### Role-Based Access

| Endpoint Pattern | Required Role | Notes |
|------------------|---------------|-------|
| `POST /auth/**` | Public | Login, Register |
| `GET /api/products/**` | Public | Browse catalog |
| `POST /api/orders/**` | CUSTOMER | Create orders |
| `GET /api/orders/{id}` | CUSTOMER (owner) | View own orders |
| `PUT /api/inventory/**` | ADMIN | Manage stock |
| `GET /actuator/**` | ADMIN | Monitoring |

---

## 📊 Observability Design

### Stack Selection (Lightweight)

| Component | Tool | Why |
|-----------|------|-----|
| **Metrics** | Micrometer + Prometheus | Spring Boot 3 native support |
| **Visualization** | Grafana | Industry standard, free |
| **Tracing** | Micrometer Tracing + Zipkin | Built into Spring Boot 3, minimal config |
| **Logging** | Logback + JSON format | Already have Logback, just format it |

**Không dùng**: ELK Stack (quá nặng), Jaeger (thêm complexity), custom APM

### Distributed Tracing Flow

```
Client Request (traceId: abc-123)
    │
    ▼
API Gateway (traceId: abc-123, spanId: span-1)
    │
    ├──► Order Service (traceId: abc-123, spanId: span-2)
    │         │
    │         ├──► Product Service (traceId: abc-123, spanId: span-3)
    │         │
    │         └──► Kafka: OrderCreated (traceId in header)
    │                   │
    │                   └──► Payment Service (traceId: abc-123, spanId: span-4)
    │
    └──► Response back to client
```

### Key Metrics to Track

```yaml
# Business Metrics
order_created_total          # Counter
order_completed_total        # Counter
payment_success_rate         # Gauge
inventory_stock_level        # Gauge per product

# Technical Metrics (auto by Micrometer)
http_server_requests_seconds # Histogram
jvm_memory_used_bytes        # Gauge
kafka_consumer_records_lag   # Gauge
```

---

## 💾 Redis Caching Strategy

### Cache Targets (High-value, low-change data)

| Data | TTL | Reason |
|------|-----|--------|
| Product catalog | 5 min | Read-heavy, changes rarely |
| Product by ID | 10 min | Frequent lookups |
| User session/roles | 15 min | Auth validation |

**Không cache**: Orders (real-time), Inventory (consistency critical), Payments

### Implementation Pattern

```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(String id) {
        return repository.findById(id);
    }

    @CacheEvict(value = "products", key = "#id")
    public void updateProduct(String id, UpdateCommand cmd) {
        // update logic
    }
}
```

---

## 📅 Implementation Phases

### Phase 3.1: API Gateway + Basic Routing (Core)

**Tasks**:
1. Create `api-gateway` module
2. Configure routes to all 5 services
3. Add request/response logging
4. Add health check aggregation
5. Update docker-compose.yml

**Deliverable**: Single entry point at port 8080

### Phase 3.2: Authentication (Core)

**Tasks**:
1. Create `auth-service` module
2. Implement User domain model
3. Implement JWT token generation/validation
4. Add login/register endpoints
5. Integrate JWT filter in API Gateway
6. Add role extraction and forwarding

**Deliverable**: Working authentication flow

### Phase 3.3: Authorization + Service Security (Core)

**Tasks**:
1. Add Spring Security to each service
2. Configure endpoint protection per role
3. Propagate user context via headers
4. Secure actuator endpoints
5. Add service-to-service token (internal calls)

**Deliverable**: Role-based access control working

### Phase 3.4: Observability (Core)

**Tasks**:
1. Add Micrometer Tracing dependencies
2. Configure Zipkin in docker-compose
3. Add Prometheus scrape config
4. Add Grafana with basic dashboards
5. Configure structured JSON logging
6. Add correlation ID propagation

**Deliverable**: End-to-end request tracing

### Phase 3.5: Caching + Rate Limiting (Enhancement)

**Tasks**:
1. Configure Redis cache in product-service
2. Add @Cacheable annotations
3. Implement rate limiting in gateway
4. Add cache metrics to monitoring

**Deliverable**: Improved performance

### Phase 3.6: Testing & Documentation

**Tasks**:
1. Integration tests for auth flow
2. Load testing with rate limiter
3. Update README and API docs
4. Create runbook for operations

---

## 🎯 Success Criteria

| Criteria | Measurement |
|----------|-------------|
| ✅ Single entry point | All traffic through port 8080 |
| ✅ JWT authentication | Login → Token → Protected endpoints |
| ✅ Role-based access | ADMIN vs CUSTOMER permissions |
| ✅ Request tracing | TraceID visible across all services |
| ✅ Metrics dashboard | Grafana shows key metrics |
| ✅ Cache hit rate | > 80% for product queries |
| ✅ Rate limiting | 429 returned when exceeded |

---

## 📁 Project Structure After Phase 3

```
ecommerce-backend/
├── common-lib/              # Shared code
├── api-gateway/             # NEW - Port 8080
├── auth-service/            # NEW - Port 8086
├── product-service/         # Port 8081
├── inventory-service/       # Port 8082
├── order-service/           # Port 8083
├── payment-service/         # Port 8084
├── notification-service/    # Port 8085
└── docker/
    ├── docker-compose.yml   # Updated with new services
    ├── prometheus/
    │   └── prometheus.yml   # Scrape config
    └── grafana/
        └── dashboards/      # Pre-configured dashboards
```

---

## 🔧 Key Technical Decisions

### 1. Why Spring Cloud Gateway (not Kong/nginx)?

| Factor | Spring Cloud Gateway | Kong | nginx |
|--------|---------------------|------|-------|
| Java ecosystem | ✅ Native | ❌ Lua/Go | ❌ C |
| Learning curve | Low (same stack) | Medium | Medium |
| JWT validation | Built-in filter | Plugin | Module |
| Observability | Micrometer native | Separate | Separate |

**Decision**: Spring Cloud Gateway - giữ tech stack đồng nhất

### 2. Why not full OAuth2 Authorization Server?

- Complexity cao (cần hiểu OAuth2 flows)
- Overkill cho internal system
- JWT đơn giản đủ cho use case hiện tại
- Có thể upgrade sau nếu cần third-party integration

### 3. Why Zipkin (not Jaeger)?

- Spring Boot 3 có built-in support qua Micrometer Tracing
- Config đơn giản hơn
- UI đủ tốt cho debugging
- Có thể swap sang Jaeger sau mà không đổi code

---

## ⚠️ Out of Scope (Avoid Over-engineering)

Những thứ **KHÔNG** làm trong Phase 3:

| Feature | Reason |
|---------|--------|
| Service Mesh (Istio) | Quá phức tạp cho 5-7 services |
| Full OAuth2 Server | Overkill, JWT đủ dùng |
| ELK Stack | Nặng, Grafana Loki nếu cần sau |
| mTLS everywhere | Chỉ cần cho production, dev dùng HTTP |
| API versioning | Chưa có breaking changes |
| Feature flags | Chưa cần ở scale hiện tại |
| A/B testing | Business requirement, không phải infra |

---

## 📋 Dependencies to Add

### api-gateway/build.gradle.kts
```kotlin
dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Rate limiting
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Observability
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
}
```

### auth-service/build.gradle.kts
```kotlin
dependencies {
    implementation(project(":common-lib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
}
```

### All services (observability)
```kotlin
// Add to root build.gradle.kts subprojects block
implementation("io.micrometer:micrometer-tracing-bridge-brave")
implementation("io.zipkin.reporter2:zipkin-reporter-brave")
implementation("io.micrometer:micrometer-registry-prometheus")
```

---

## 📊 Progress Tracking

See [PHASE3_TASK_TRACKER.md](../tracking/PHASE3_TASK_TRACKER.md) for detailed task tracking.
