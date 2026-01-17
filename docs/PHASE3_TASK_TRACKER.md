# 📊 Phase 3: Enterprise Level - Task Tracker

**Started**: TBD
**Target Completion**: TBD
**Status**: ⬜ Not Started

---

## Quick Status

| Phase | Description | Progress |
|-------|-------------|----------|
| 3.1 | API Gateway + Routing | ⬜ 0% |
| 3.2 | Authentication | ⬜ 0% |
| 3.3 | Authorization + Service Security | ⬜ 0% |
| 3.4 | Observability | ⬜ 0% |
| 3.5 | Caching + Rate Limiting | ⬜ 0% |
| 3.6 | Testing & Documentation | ⬜ 0% |

---

## 📋 Phase 3.1: API Gateway + Routing

### Project Scaffold
- [ ] Create `api-gateway` module directory structure
- [ ] Add `api-gateway` to `settings.gradle.kts`
- [ ] Create `build.gradle.kts` with Spring Cloud Gateway dependencies
- [ ] Create `ApiGatewayApplication.java`
- [ ] Add `application.yml` with Port 8080

### Route Configuration
- [ ] Configure route to product-service (`/api/products/**`)
- [ ] Configure route to inventory-service (`/api/inventory/**`)
- [ ] Configure route to order-service (`/api/orders/**`)
- [ ] Configure route to payment-service (`/api/payments/**`)
- [ ] Configure route to notification-service (`/api/notifications/**`)
- [ ] Configure route to auth-service (`/auth/**`)

### Gateway Features
- [ ] Add global request logging filter
- [ ] Add response time logging
- [ ] Configure CORS for frontend
- [ ] Add health check endpoint aggregation
- [ ] Configure circuit breaker (optional, Resilience4j)

### Docker Integration
- [ ] Add `api-gateway` to `docker-compose.yml`
- [ ] Expose only port 8080 externally
- [ ] Test routing to all services

---

## 📋 Phase 3.2: Authentication

### Project Scaffold
- [ ] Create `auth-service` module directory structure
- [ ] Add `auth-service` to `settings.gradle.kts`
- [ ] Create `build.gradle.kts` for auth-service
- [ ] Create `AuthServiceApplication.java`
- [ ] Add `application.yml` with Port 8086

### Domain Layer
- [ ] Create `User` aggregate root
- [ ] Create `UserId` value object
- [ ] Create `Role` enum (ADMIN, CUSTOMER, SERVICE)
- [ ] Create `RefreshToken` entity
- [ ] Create `UserRepository` port interface

### Application Layer
- [ ] Create `AuthService` application service
- [ ] Implement `register()` method
- [ ] Implement `login()` method
- [ ] Implement `refreshToken()` method
- [ ] Implement `logout()` method
- [ ] Create DTOs (LoginRequest, LoginResponse, RegisterRequest)

### Infrastructure Layer - Security
- [ ] Create `JwtTokenProvider` class
- [ ] Implement JWT generation with claims (sub, roles, exp)
- [ ] Implement JWT validation
- [ ] Implement JWT parsing
- [ ] Configure secret key management (env variable)

### Infrastructure Layer - Persistence
- [ ] Create `UserEntity` JPA entity
- [ ] Create `RefreshTokenEntity` JPA entity
- [ ] Create `JpaUserRepository`
- [ ] Create Flyway migration `V1__create_user_tables.sql`

### Infrastructure Layer - Web
- [ ] Create `AuthController`
- [ ] `POST /auth/register` endpoint
- [ ] `POST /auth/login` endpoint
- [ ] `POST /auth/refresh` endpoint
- [ ] `POST /auth/logout` endpoint

### Gateway Integration
- [ ] Create `JwtAuthenticationFilter` in api-gateway
- [ ] Extract JWT from Authorization header
- [ ] Validate token signature and expiration
- [ ] Extract user info and roles
- [ ] Forward user context to downstream services via headers

### Docker Integration
- [ ] Add `auth-service` to `docker-compose.yml`
- [ ] Update `init-databases.sh` to create `auth_db`

---

## 📋 Phase 3.3: Authorization + Service Security

### Common Security Library
- [ ] Add security config to `common-lib`
- [ ] Create `SecurityConstants` class
- [ ] Create `UserContext` record (userId, username, roles)
- [ ] Create `UserContextHolder` (ThreadLocal)
- [ ] Create `UserContextFilter` for extracting headers

### Product Service Security
- [ ] Add Spring Security dependency
- [ ] Create `SecurityConfig` class
- [ ] Configure: GET `/api/products/**` = permitAll
- [ ] Configure: POST/PUT/DELETE `/api/products/**` = ADMIN
- [ ] Extract user context from gateway headers

### Inventory Service Security
- [ ] Add Spring Security dependency
- [ ] Create `SecurityConfig` class
- [ ] Configure: GET `/api/inventory/**` = permitAll
- [ ] Configure: POST/PUT `/api/inventory/**` = ADMIN or SERVICE
- [ ] Extract user context from gateway headers

### Order Service Security
- [ ] Add Spring Security dependency
- [ ] Create `SecurityConfig` class
- [ ] Configure: POST `/api/orders` = CUSTOMER
- [ ] Configure: GET `/api/orders/{id}` = CUSTOMER (owner check)
- [ ] Configure: GET `/api/orders` = ADMIN (list all)
- [ ] Add owner validation in OrderService

### Payment Service Security
- [ ] Add Spring Security dependency
- [ ] Configure: All endpoints = SERVICE only (internal)

### Notification Service Security
- [ ] Add Spring Security dependency
- [ ] Configure: All endpoints = SERVICE only (internal)

### Service-to-Service Auth
- [ ] Create internal service token mechanism
- [ ] Add token to ProductServiceClient in order-service
- [ ] Add token to InventoryServiceClient in order-service
- [ ] Validate SERVICE role in target services

### Actuator Security
- [ ] Secure `/actuator/**` endpoints (ADMIN only)
- [ ] Keep `/actuator/health` public for load balancer

---

## 📋 Phase 3.4: Observability

### Dependencies Setup
- [ ] Add Micrometer Tracing to root `build.gradle.kts`
- [ ] Add Zipkin reporter dependency
- [ ] Add Prometheus registry dependency
- [ ] Verify all services pick up dependencies

### Distributed Tracing
- [ ] Configure Zipkin URL in all services
- [ ] Add trace ID propagation in Kafka messages
- [ ] Add trace ID propagation in HTTP headers
- [ ] Test trace visibility across services
- [ ] Verify trace continues through Kafka consumers

### Metrics
- [ ] Configure Prometheus endpoint (`/actuator/prometheus`)
- [ ] Add custom business metrics:
  - [ ] `order_created_total` counter
  - [ ] `order_completed_total` counter
  - [ ] `payment_success_total` / `payment_failed_total`
  - [ ] `inventory_reservation_total`
- [ ] Add latency histograms for key operations

### Logging
- [ ] Configure Logback JSON format
- [ ] Add MDC for traceId, spanId
- [ ] Add MDC for userId (from context)
- [ ] Standardize log format across services

### Infrastructure
- [ ] Add Zipkin to `docker-compose.yml`
- [ ] Add Prometheus to `docker-compose.yml`
- [ ] Create `prometheus.yml` scrape config
- [ ] Add Grafana to `docker-compose.yml`
- [ ] Create basic Grafana dashboard for:
  - [ ] Request rates per service
  - [ ] Error rates
  - [ ] Latency percentiles
  - [ ] JVM metrics

---

## 📋 Phase 3.5: Caching + Rate Limiting

### Redis Caching - Product Service
- [ ] Add Spring Cache + Redis dependencies
- [ ] Create `CacheConfig` class
- [ ] Add `@Cacheable("products")` to `getProductById()`
- [ ] Add `@Cacheable("product-list")` to `getAllProducts()`
- [ ] Add `@CacheEvict` to update/delete operations
- [ ] Configure TTL (5 minutes for list, 10 minutes for single)
- [ ] Add cache metrics to Prometheus

### Rate Limiting - API Gateway
- [ ] Add Redis rate limiter dependency
- [ ] Create `RateLimitConfig` class
- [ ] Configure rate limit: 100 requests/minute per IP
- [ ] Configure rate limit: 1000 requests/minute per authenticated user
- [ ] Add rate limit headers to response
- [ ] Return 429 when exceeded
- [ ] Add rate limit metrics

### Performance Testing
- [ ] Create load test script (k6 or wrk)
- [ ] Measure cache hit rate
- [ ] Verify rate limiter works correctly
- [ ] Document performance improvements

---

## 📋 Phase 3.6: Testing & Documentation

### Unit Tests
- [ ] JwtTokenProvider tests
- [ ] AuthService tests
- [ ] JwtAuthenticationFilter tests
- [ ] UserContextFilter tests
- [ ] Cache eviction tests

### Integration Tests
- [ ] Auth flow: register → login → access protected endpoint
- [ ] Unauthorized access returns 401
- [ ] Forbidden access returns 403
- [ ] Rate limiter returns 429
- [ ] Trace ID propagation test

### End-to-End Tests
- [ ] Full flow: Login → Create Order → Verify tracing
- [ ] Admin vs Customer access
- [ ] Token refresh flow
- [ ] Cache invalidation on update

### Documentation
- [ ] Update `MASTER_PLAN.md` with Phase 3 status
- [ ] Update `README.md` with:
  - [ ] New services (api-gateway, auth-service)
  - [ ] How to login and get token
  - [ ] How to access protected endpoints
  - [ ] Monitoring URLs (Grafana, Zipkin)
- [ ] Create `docs/SECURITY.md` with:
  - [ ] Authentication flow
  - [ ] Role descriptions
  - [ ] How to add new protected endpoints
- [ ] Create `docs/OBSERVABILITY.md` with:
  - [ ] How to view traces
  - [ ] Key metrics explained
  - [ ] How to add custom metrics

---

## 🗒️ Notes & Decisions Log

| Date | Decision/Note |
|------|---------------|
| TBD | Phase 3 planning completed |

---

## 🚧 Blockers & Dependencies

| Blocker | Status | Resolution |
|---------|--------|------------|
| Phase 2 completion | ⏳ | Phase 2.4 still in progress |

---

## 📈 Progress Summary

- **Total Tasks**: ~120
- **Completed**: 0
- **In Progress**: 0
- **Blocked**: 0
- **Overall Progress**: 0%
