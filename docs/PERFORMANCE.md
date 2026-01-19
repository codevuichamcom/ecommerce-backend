# ⚡ Performance Guide

Performance targets, benchmarks, and optimization strategies for the E-commerce Backend.

---

## Table of Contents

1. [Performance Overview](#performance-overview)
2. [SLA Targets](#sla-targets)
3. [Performance Benchmarks](#performance-benchmarks)
4. [Optimization Strategies](#optimization-strategies)
5. [Load Testing](#load-testing)
6. [Performance Monitoring](#performance-monitoring)

---

## Performance Overview

### Performance Goals

| Metric | Target | Measurement |
|--------|--------|-------------|
| **API Latency (P95)** | < 500ms | 95th percentile response time |
| **API Latency (P99)** | < 1000ms | 99th percentile response time |
| **Throughput** | 1000 req/s | Requests per second per service |
| **Availability** | 99.9% | Uptime percentage |
| **Error Rate** | < 1% | Failed requests percentage |

### Performance Principles

- **Optimize for P95, not average**: Focus on tail latency
- **Measure everything**: You can't improve what you don't measure
- **Test under load**: Performance degrades under real-world conditions
- **Plan for scale**: Design for 10x current load

---

## SLA Targets

### Service Level Agreements

#### API Gateway

| Metric | SLA | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.9% | 30 days |
| **Latency (P95)** | < 200ms | 5 minutes |
| **Latency (P99)** | < 500ms | 5 minutes |
| **Throughput** | 5000 req/s | Peak capacity |

**Error Budget**: 43.2 minutes downtime per month

#### Product Service

| Metric | SLA | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.5% | 30 days |
| **Latency (P95)** | < 300ms | 5 minutes |
| **Latency (P99)** | < 800ms | 5 minutes |
| **Cache Hit Rate** | > 80% | 1 hour |

#### Order Service

| Metric | SLA | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.9% | 30 days |
| **Latency (P95)** | < 1000ms | 5 minutes |
| **Order Completion Rate** | > 95% | 1 hour |
| **Saga Success Rate** | > 98% | 1 hour |

#### Payment Service

| Metric | SLA | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.9% | 30 days |
| **Latency (P95)** | < 2000ms | 5 minutes |
| **Payment Success Rate** | > 95% | 1 hour |

#### Inventory Service

| Metric | SLA | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.5% | 30 days |
| **Latency (P95)** | < 500ms | 5 minutes |
| **Stock Reservation Success** | > 99% | 1 hour |

---

## Performance Benchmarks

### Current Performance (Development)

**Test Environment**:
- 1 instance per service
- PostgreSQL (single node)
- Kafka (single broker)
- 4 CPU cores, 8GB RAM

**Results**:

| Endpoint | P50 | P95 | P99 | Throughput |
|----------|-----|-----|-----|------------|
| `GET /api/products` | 45ms | 120ms | 250ms | 500 req/s |
| `GET /api/products/{id}` | 15ms | 35ms | 80ms | 1000 req/s |
| `POST /api/products` | 80ms | 180ms | 350ms | 200 req/s |
| `POST /api/orders` | 450ms | 1200ms | 2500ms | 50 req/s |
| `GET /api/orders/{id}` | 60ms | 150ms | 300ms | 300 req/s |

### Target Performance (Production)

**Production Environment**:
- 3 instances per service (auto-scaling)
- PostgreSQL cluster (1 primary + 2 replicas)
- Kafka cluster (5 brokers)
- 8 CPU cores, 16GB RAM per instance

**Targets**:

| Endpoint | P50 | P95 | P99 | Throughput |
|----------|-----|-----|-----|------------|
| `GET /api/products` | < 30ms | < 100ms | < 200ms | 2000 req/s |
| `GET /api/products/{id}` | < 10ms | < 25ms | < 50ms | 5000 req/s |
| `POST /api/products` | < 50ms | < 150ms | < 300ms | 500 req/s |
| `POST /api/orders` | < 300ms | < 1000ms | < 2000ms | 200 req/s |
| `GET /api/orders/{id}` | < 40ms | < 120ms | < 250ms | 1000 req/s |

---

## Optimization Strategies

### 1. Database Optimization

#### Connection Pooling

**HikariCP Configuration**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50      # Increase for production
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Pool Size Formula**:
```
pool_size = (core_count × 2) + effective_spindle_count
Example: (8 × 2) + 4 = 20 connections
```

#### Query Optimization

**Use Indexes**:
```sql
-- Add indexes for frequently queried columns
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Composite index for common queries
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
```

**Avoid N+1 Queries**:
```java
// ❌ Bad - N+1 query
@OneToMany(mappedBy = "order")
private List<OrderItem> items;

// ✅ Good - JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") String id);
```

**Use Pagination**:
```java
// ✅ Always paginate large result sets
Page<Product> products = productRepository.findAll(
    PageRequest.of(page, size, Sort.by("createdAt").descending())
);
```

**Database Query Monitoring**:
```sql
-- PostgreSQL: Find slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### 2. Caching Strategy

#### Redis Caching

**Product Caching** (10 minutes TTL):
```java
@Cacheable(value = "products", key = "#id")
public Product findById(String id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
}

@CacheEvict(value = "products", key = "#id")
public void updateProduct(String id, UpdateProductRequest request) {
    // Update logic
}
```

**Cache Configuration**:
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes
      cache-null-values: false
```

**Cache Hit Rate Target**: > 80%

#### HTTP Caching

**Cache-Control Headers**:
```java
@GetMapping("/products/{id}")
public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
    ProductResponse product = productService.findById(id);
    
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
        .eTag(product.version())
        .body(product);
}
```

### 3. Async Processing

#### Virtual Threads (Java 21)

**Already Enabled**:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Benefits**:
- Lightweight threads (millions possible)
- Better resource utilization
- Simplified async code

#### Kafka Async Processing

**Non-blocking Event Publishing**:
```java
@Async
public CompletableFuture<Void> publishEvent(DomainEvent event) {
    return CompletableFuture.runAsync(() -> {
        outboxService.publish(event);
    });
}
```

### 4. API Optimization

#### Response Compression

**Enable GZIP**:
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024
```

#### Pagination

**Always Paginate**:
```java
@GetMapping("/products")
public Page<ProductResponse> getProducts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    return productService.findAll(PageRequest.of(page, size));
}
```

#### Field Selection

**Allow clients to select fields**:
```java
@GetMapping("/products")
public List<ProductResponse> getProducts(
    @RequestParam(required = false) String fields
) {
    // Return only requested fields
    // Example: ?fields=id,name,price
}
```

### 5. Kafka Optimization

#### Producer Configuration

```yaml
spring:
  kafka:
    producer:
      batch-size: 16384
      linger-ms: 10
      compression-type: snappy
      acks: all
      buffer-memory: 33554432
```

#### Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      fetch-min-size: 1
      fetch-max-wait: 500
      max-poll-records: 500
    listener:
      concurrency: 3  # Parallel consumers
```

---

## Load Testing

### Tools

**Gatling**:
```scala
class OrderLoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
  
  val scn = scenario("Create Order")
    .exec(
      http("Create Order")
        .post("/api/v1/orders")
        .header("Authorization", "Bearer ${token}")
        .body(StringBody("""{"customerId":"customer-123","items":[{"productId":"product-1","quantity":2}]}"""))
        .check(status.is(201))
    )
  
  setUp(
    scn.inject(
      rampUsersPerSec(10) to 100 during (60 seconds),
      constantUsersPerSec(100) during (300 seconds)
    )
  ).protocols(httpProtocol)
}
```

**Apache JMeter**:
- GUI for creating test plans
- Distributed load testing
- Real-time results

**k6**:
```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  let res = http.get('http://localhost:8081/api/v1/products');
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
}
```

### Load Test Scenarios

**Scenario 1: Normal Load**
- 100 concurrent users
- Duration: 10 minutes
- Expected: All SLAs met

**Scenario 2: Peak Load**
- 500 concurrent users
- Duration: 5 minutes
- Expected: P95 < 1000ms, error rate < 5%

**Scenario 3: Stress Test**
- Ramp up to 1000 users
- Duration: 15 minutes
- Expected: Identify breaking point

**Scenario 4: Spike Test**
- Sudden spike from 100 to 1000 users
- Duration: 2 minutes
- Expected: System recovers gracefully

---

## Performance Monitoring

### Key Metrics

**Latency Metrics**:
```promql
# P95 latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# P99 latency
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))

# Average latency
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])
```

**Throughput Metrics**:
```promql
# Requests per second
rate(http_server_requests_seconds_count[5m])

# Requests per minute
rate(http_server_requests_seconds_count[1m]) * 60
```

**Error Rate**:
```promql
# Error rate percentage
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
/ 
sum(rate(http_server_requests_seconds_count[5m])) * 100
```

**Database Performance**:
```promql
# Connection pool utilization
(hikaricp_connections_active / hikaricp_connections_max) * 100

# Query duration
rate(hikaricp_connections_acquire_seconds_sum[5m]) / rate(hikaricp_connections_acquire_seconds_count[5m])
```

### Grafana Dashboards

**Performance Dashboard Panels**:
1. Request rate (req/s)
2. P50/P95/P99 latency
3. Error rate (%)
4. Database connection pool
5. Cache hit rate
6. JVM heap usage
7. GC pause time
8. Kafka consumer lag

---

## Performance Checklist

### Development

- [ ] Enable query logging
- [ ] Profile slow endpoints
- [ ] Optimize N+1 queries
- [ ] Add database indexes
- [ ] Enable caching
- [ ] Use pagination

### Pre-Production

- [ ] Run load tests
- [ ] Verify SLA targets met
- [ ] Check database query performance
- [ ] Optimize connection pools
- [ ] Enable compression
- [ ] Configure auto-scaling

### Production

- [ ] Monitor P95/P99 latency
- [ ] Track error rates
- [ ] Monitor cache hit rates
- [ ] Review slow query logs
- [ ] Analyze Zipkin traces
- [ ] Capacity planning

---

## Performance Troubleshooting

### High Latency

**Diagnosis**:
1. Check Zipkin for slow spans
2. Review database query logs
3. Check external API calls
4. Verify cache hit rate

**Solutions**:
- Add caching
- Optimize queries
- Add indexes
- Use async processing

### High Error Rate

**Diagnosis**:
1. Check error logs
2. Review Prometheus metrics
3. Check database connections
4. Verify Kafka consumer lag

**Solutions**:
- Increase connection pool
- Scale horizontally
- Fix application bugs
- Add circuit breakers

### Low Throughput

**Diagnosis**:
1. Check CPU/memory usage
2. Review thread pool size
3. Check database connections
4. Verify network bandwidth

**Solutions**:
- Scale vertically (more CPU/RAM)
- Scale horizontally (more instances)
- Optimize resource usage
- Enable virtual threads

---

## Capacity Planning

### Current Capacity

| Service | Instances | CPU | Memory | Max Throughput |
|---------|-----------|-----|--------|----------------|
| Gateway | 2 | 4 cores | 8GB | 2000 req/s |
| Product | 2 | 4 cores | 8GB | 1000 req/s |
| Order | 2 | 4 cores | 8GB | 100 req/s |
| Payment | 2 | 4 cores | 8GB | 100 req/s |
| Inventory | 2 | 4 cores | 8GB | 500 req/s |

### Growth Projections

**6 Months**:
- Expected traffic: 3x current
- Required instances: 6 per service
- Estimated cost: $5000/month

**12 Months**:
- Expected traffic: 10x current
- Required instances: 20 per service
- Estimated cost: $15000/month

---

## Next Steps

- [Observability](OBSERVABILITY.md) - Performance monitoring
- [Troubleshooting](TROUBLESHOOTING.md) - Performance issues
- [Deployment](DEPLOYMENT.md) - Auto-scaling configuration

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-19  
**Maintained By**: Performance Team
