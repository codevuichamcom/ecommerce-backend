# 📊 Phase 3: Enterprise Level - Task Tracker

**Started**: 2026-01-17
**Target Completion**: TBD
**Status**: 🔄 In Progress

---

## Quick Status

| Phase | Description | Progress |
|-------|-------------|----------|
| 3.1 | API Gateway + Routing | ✅ 85% |
| 3.2 | Authentication | ✅ 100% |
| 3.3 | Authorization + Service Security | ✅ 100% |
| 3.4 | Observability | ⬜ 0% |
| 3.5 | Caching + Rate Limiting | ⬜ 0% |
| 3.6 | Testing & Documentation | ⬜ 0% |

---

## 📋 Phase 3.1: API Gateway + Routing

### Project Scaffold
- [x] Create `api-gateway` module directory structure
- [x] Add `api-gateway` to `settings.gradle.kts`
- [x] Create `build.gradle.kts` with Spring Cloud Gateway dependencies
- [x] Create `ApiGatewayApplication.java`
- [x] Add `application.yml` with Port 8080

### Route Configuration
- [x] Configure route to product-service (`/api/products/**`)
- [x] Configure route to inventory-service (`/api/inventory/**`)
- [x] Configure route to order-service (`/api/orders/**`)
- [x] Configure route to payment-service (`/api/payments/**`)
- [x] Configure route to notification-service (`/api/notifications/**`)
- [x] Configure route to auth-service (`/auth/**`) - Completed in Phase 3.2

### Gateway Features
- [x] Add global request logging filter
- [x] Add response time logging
- [x] Configure CORS for frontend
- [x] Add health check endpoint aggregation
- [ ] Configure circuit breaker (optional, Resilience4j) - Deferred to Phase 3.6

### Docker Integration
- [x] Add `api-gateway` to `docker-compose.yml`
- [x] Expose only port 8080 externally
- [ ] Test routing to all services

---

## 📋 Phase 3.2: Authentication

### Project Scaffold
- [x] Create `auth-service` module directory structure
- [x] Add `auth-service` to `settings.gradle.kts`
- [x] Create `build.gradle.kts` for auth-service
- [x] Create `AuthServiceApplication.java`
- [x] Add `application.yml` with Port 8086

### Domain Layer
- [x] Create `User` aggregate root
- [x] Create `UserId` value object
- [x] Create `Role` enum (ADMIN, CUSTOMER, SERVICE)
- [x] Create `RefreshToken` entity
- [x] Create `UserRepository` port interface

### Application Layer
- [x] Create `AuthService` application service
- [x] Implement `register()` method
- [x] Implement `login()` method
- [x] Implement `refreshToken()` method
- [x] Implement `logout()` method
- [x] Create DTOs (LoginRequest, LoginResponse, RegisterRequest)

### Infrastructure Layer - Security
- [x] Create `JwtTokenProvider` class
- [x] Implement JWT generation with claims (sub, roles, exp)
- [x] Implement JWT validation
- [x] Implement JWT parsing
- [x] Configure secret key management (env variable)

### Infrastructure Layer - Persistence
- [x] Create `UserEntity` JPA entity
- [x] Create `RefreshTokenEntity` JPA entity
- [x] Create `JpaUserRepository`
- [x] Create Flyway migration `V1__create_auth_tables.sql`

### Infrastructure Layer - Web
- [x] Create `AuthController`
- [x] `POST /auth/register` endpoint
- [x] `POST /auth/login` endpoint
- [x] `POST /auth/refresh` endpoint
- [x] `POST /auth/logout` endpoint

### Gateway Integration
- [x] Create `JwtAuthenticationFilter` in api-gateway
- [x] Extract JWT from Authorization header
- [x] Validate token signature and expiration
- [x] Extract user info and roles
- [x] Forward user context to downstream services via headers

### Docker Integration
- [x] Add `auth-service` to `docker-compose.yml`
- [x] Update `init-databases.sh` to create `auth_db`

---

## 📋 Phase 3.3: Authorization + Service Security

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
