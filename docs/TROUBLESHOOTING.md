# 🔧 Troubleshooting Guide

Common issues and solutions for the E-commerce Backend microservices.

---

## Quick Diagnosis

### Service Health Check

```bash
# Check all services health
for port in 8080 8081 8082 8083 8084 8085 8086; do
  echo "Port $port:"
  curl -s http://localhost:$port/actuator/health | jq '.status'
done
```

### Infrastructure Health

```bash
# Check Docker containers
docker-compose ps

# Check PostgreSQL
docker exec -it postgres pg_isready

# Check Kafka
docker exec -it kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Check Redis
docker exec -it redis redis-cli ping
```

---

## Common Issues

### 1. Service Won't Start

#### Symptom
```
Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
```

#### Possible Causes & Solutions

**A. Port Already in Use**

```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8091
```

**B. Database Connection Failed**

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U postgres -d product_db

# Verify credentials in application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: postgres
    password: postgres
```

**C. Missing Database Schema**

```bash
# Check Flyway migrations
./gradlew :product-service:flywayInfo

# Run migrations manually
./gradlew :product-service:flywayMigrate

# Repair failed migration
./gradlew :product-service:flywayRepair
```

**D. Kafka Not Available**

```bash
# Check Kafka is running
docker-compose logs kafka

# Restart Kafka
docker-compose restart zookeeper kafka

# Verify bootstrap servers
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

### 2. Database Connection Pool Exhausted

#### Symptom
```
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

#### Root Cause
Too many concurrent requests or connection leaks.

#### Solutions

**A. Increase Pool Size**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase from 10
      minimum-idle: 10
      connection-timeout: 30000
```

**B. Check for Connection Leaks**

```bash
# Enable connection leak detection
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000  # 60 seconds
```

**C. Monitor Active Connections**

```sql
-- PostgreSQL: Check active connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'product_db';

-- Check max connections
SHOW max_connections;
```

**D. Restart Service**

```bash
# Graceful restart
./gradlew :product-service:bootRun
```

**Related Runbook**: [Database Connection Pool](runbooks/database-connection-pool.md) *(Coming Soon)*

---

### 3. Kafka Consumer Lag

#### Symptom
```
Consumer group 'order-service-group' has lag of 10000 messages
```

#### Root Cause
Consumer processing slower than producer rate.

#### Solutions

**A. Check Consumer Status**

```bash
# View consumer groups
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Check lag
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group order-service-group \
  --describe
```

**B. Increase Concurrency**

```yaml
spring:
  kafka:
    listener:
      concurrency: 3  # Increase from 1
```

**C. Optimize Consumer Logic**

- Remove blocking I/O operations
- Use batch processing
- Enable async processing

**D. Scale Horizontally**

```bash
# Run multiple instances
./gradlew :order-service:bootRun &
./gradlew :order-service:bootRun &
```

**Related Runbook**: [Kafka Consumer Lag](runbooks/kafka-consumer-lag.md) *(Coming Soon)*

---

### 4. Order Stuck in PENDING Status

#### Symptom
Order created but never moves to CONFIRMED or CANCELLED.

#### Root Cause
Saga orchestration failure or event not consumed.

#### Diagnosis

**A. Check Order Saga State**

```sql
-- Connect to order_db
psql -h localhost -U postgres -d order_db

-- Check saga state
SELECT order_id, state, last_error, updated_at 
FROM order_sagas 
WHERE order_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H';
```

**B. Check Outbox Events**

```sql
-- Check if events are published
SELECT * FROM outbox_events 
WHERE aggregate_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H'
ORDER BY created_at DESC;
```

**C. Check Kafka Topics**

```bash
# Consume order events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  | grep "01HQZX3Y4Z5A6B7C8D9E0F1G2H"
```

#### Solutions

**A. Republish Events**

```sql
-- Mark event as PENDING to retry
UPDATE outbox_events 
SET status = 'PENDING', published_at = NULL
WHERE aggregate_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H';
```

**B. Manual Compensation**

```bash
# Cancel stuck order via API
curl -X POST http://localhost:8083/api/v1/orders/01HQZX3Y4Z5A6B7C8D9E0F1G2H/cancel \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Related Runbook**: [Order Stuck Pending](runbooks/order-stuck-pending.md) *(Coming Soon)*

---

### 5. High Memory Usage / OutOfMemoryError

#### Symptom
```
java.lang.OutOfMemoryError: Java heap space
```

#### Root Cause
Memory leak, large result sets, or insufficient heap size.

#### Diagnosis

**A. Check Memory Usage**

```bash
# JVM memory info
jcmd <PID> VM.native_memory summary

# Heap dump
jmap -dump:format=b,file=heap.bin <PID>

# Analyze with Eclipse MAT or VisualVM
```

**B. Check Metrics**

```bash
# Prometheus metrics
curl http://localhost:8081/actuator/prometheus | grep jvm_memory
```

#### Solutions

**A. Increase Heap Size**

```bash
# Run with more memory
./gradlew :product-service:bootRun -Dspring-boot.run.jvmArguments="-Xmx2g -Xms1g"
```

**B. Enable Pagination**

```java
// Avoid loading all results
Page<Product> products = productRepository.findAll(PageRequest.of(0, 20));
```

**C. Fix N+1 Queries**

```java
// Use JOIN FETCH
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") String id);
```

**D. Clear Caches**

```bash
# Clear Redis cache
docker exec -it redis redis-cli FLUSHDB
```

**Related Runbook**: [High Memory Usage](runbooks/high-memory-usage.md) *(Coming Soon)*

---

### 6. Redis Connection Timeout

#### Symptom
```
io.lettuce.core.RedisCommandTimeoutException: Command timed out after 2 second(s)
```

#### Root Cause
Redis overloaded, network issues, or slow queries.

#### Solutions

**A. Check Redis Status**

```bash
# Connect to Redis
docker exec -it redis redis-cli

# Check info
INFO

# Check slow log
SLOWLOG GET 10
```

**B. Increase Timeout**

```yaml
spring:
  data:
    redis:
      timeout: 5000ms  # Increase from 2000ms
```

**C. Restart Redis**

```bash
docker-compose restart redis
```

**D. Clear Redis Data**

```bash
# WARNING: Deletes all data
docker exec -it redis redis-cli FLUSHALL
```

---

### 7. JWT Token Expired

#### Symptom
```
HTTP 401 Unauthorized
{
  "error": "invalid_token",
  "error_description": "Token has expired"
}
```

#### Solutions

**A. Refresh Token**

```bash
curl -X POST http://localhost:8086/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

**B. Login Again**

```bash
curl -X POST http://localhost:8086/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer@example.com",
    "password": "password123"
  }'
```

**C. Increase Token Validity** (Development Only)

```yaml
jwt:
  access-token-validity-seconds: 7200  # 2 hours
```

---

### 8. Optimistic Locking Failure

#### Symptom
```
org.springframework.orm.ObjectOptimisticLockingFailureException: 
Row was updated or deleted by another transaction
```

#### Root Cause
Concurrent updates to same entity (inventory, payment).

#### Solutions

**A. Retry Request**

The client should retry the request. The operation is idempotent.

**B. Implement Retry Logic**

```java
@Retryable(
    value = ObjectOptimisticLockingFailureException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public void reserveStock(String productId, int quantity) {
    // Implementation
}
```

**C. Check Version Conflicts**

```sql
-- Check current version
SELECT id, product_id, version FROM inventory 
WHERE product_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H';
```

---

### 9. Flyway Migration Failed

#### Symptom
```
FlywayException: Validate failed: Migration checksum mismatch
```

#### Root Cause
Migration file modified after being applied.

#### Solutions

**A. Repair Flyway**

```bash
./gradlew :product-service:flywayRepair
```

**B. Check Migration History**

```bash
./gradlew :product-service:flywayInfo
```

**C. Baseline Existing Database**

```bash
./gradlew :product-service:flywayBaseline
```

**D. Clean and Migrate** (Development Only)

```bash
# WARNING: Deletes all data
./gradlew :product-service:flywayClean
./gradlew :product-service:flywayMigrate
```

---

### 10. Slow API Response Times

#### Symptom
API requests taking > 1 second.

#### Diagnosis

**A. Check Distributed Tracing**

```bash
# Open Zipkin
open http://localhost:9411

# Find slow traces
# Look for database queries, external API calls
```

**B. Check Database Query Performance**

```sql
-- Enable query logging
SET log_statement = 'all';

-- Check slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

**C. Check Metrics**

```bash
curl http://localhost:8081/actuator/metrics/http.server.requests | jq
```

#### Solutions

**A. Add Database Indexes**

```sql
-- Example: Index on frequently queried column
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```

**B. Enable Caching**

```java
@Cacheable(value = "products", key = "#id")
public Product findById(String id) {
    return productRepository.findById(id).orElseThrow();
}
```

**C. Optimize Queries**

```java
// Use projection for read-only queries
interface ProductSummary {
    String getId();
    String getName();
    BigDecimal getPrice();
}

List<ProductSummary> findAllProjectedBy();
```

**D. Enable Virtual Threads** (Already enabled)

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

---

## Debugging Techniques

### Enable Debug Logging

**Temporary** (via environment variable):
```bash
export LOGGING_LEVEL_COM_ECOMMERCE=DEBUG
./gradlew :product-service:bootRun
```

**Permanent** (application.yml):
```yaml
logging:
  level:
    com.ecommerce: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.kafka: DEBUG
```

### Remote Debugging

**1. Start service with debug enabled**:
```bash
./gradlew :product-service:bootRun --debug-jvm
```

**2. Attach debugger**:
- IntelliJ: Run > Attach to Process
- Port: 5005 (default)

### View Application Logs

```bash
# Real-time logs
tail -f logs/product-service.log

# Search logs
grep "ERROR" logs/product-service.log

# View with context
grep -A 5 -B 5 "OutOfMemoryError" logs/product-service.log
```

### Analyze Thread Dumps

```bash
# Generate thread dump
jstack <PID> > thread-dump.txt

# Look for deadlocks
grep -A 10 "deadlock" thread-dump.txt
```

---

## Monitoring & Alerting

### Grafana Dashboards

Access: http://localhost:3000 (admin/admin)

**Key Dashboards**:
- JVM Metrics
- HTTP Request Metrics
- Database Connection Pool
- Kafka Consumer Lag

### Prometheus Queries

Access: http://localhost:9090

**Useful Queries**:
```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Database connections
hikaricp_connections_active

# Kafka consumer lag
kafka_consumer_lag
```

---

## Getting Help

### Escalation Path

1. **Check this guide** for common issues
2. **Check logs** for error messages
3. **Check Zipkin** for distributed traces
4. **Ask in Slack** `#ecommerce-backend`
5. **Create incident** if production issue
6. **Contact on-call** for P0/P1 incidents

### Useful Commands Cheat Sheet

```bash
# Service health
curl http://localhost:8081/actuator/health

# Restart infrastructure
docker-compose restart

# View Kafka topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Connect to database
psql -h localhost -U postgres -d product_db

# Check Redis
docker exec -it redis redis-cli INFO

# View logs
docker-compose logs -f product-service

# Check Java processes
jps -l

# Memory usage
jcmd <PID> GC.heap_info
```

---

## Next Steps

- [Runbooks](runbooks/) *(Coming Soon)* - Detailed incident response procedures
- [Observability Guide](OBSERVABILITY.md) - Monitoring and tracing
- [Development Guide](DEVELOPMENT.md) - Local debugging
- [Configuration Reference](CONFIGURATION.md) - Environment variables

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-19  
**Maintained By**: SRE Team
