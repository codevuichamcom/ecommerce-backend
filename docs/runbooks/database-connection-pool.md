# Runbook: Database Connection Pool Exhausted

**Alert Name**: `DatabaseConnectionPoolExhausted`  
**Severity**: Critical  
**MTTR Target**: 5 minutes

---

## Alert Details

**Trigger**:
```promql
hikaricp_connections_active / hikaricp_connections_max > 0.9
```

**Symptoms**:
- `Connection is not available` errors
- API requests timing out
- 503 Service Unavailable responses

---

## Immediate Actions

### 1. Check Pool Status

```bash
# Prometheus metrics
curl http://localhost:8081/actuator/prometheus | grep hikaricp

# Expected output
hikaricp_connections_active{pool="HikariPool-1"} 18
hikaricp_connections_max{pool="HikariPool-1"} 20
```

### 2. Check for Connection Leaks

```bash
# Enable leak detection (if not already)
spring:
  datasource:
    hikaricp:
      leak-detection-threshold: 60000  # 60 seconds
```

### 3. Restart Service (Quick Fix)

```bash
# Kubernetes
kubectl rollout restart deployment/product-service

# Docker Compose
docker-compose restart product-service
```

---

## Root Cause Analysis

### Check Active Connections

```sql
-- PostgreSQL
SELECT count(*) FROM pg_stat_activity 
WHERE datname = 'product_db';

-- Check long-running queries
SELECT pid, now() - query_start as duration, query
FROM pg_stat_activity
WHERE state = 'active'
ORDER BY duration DESC;
```

### Common Causes

1. **Connection Leaks**: Not closing connections
2. **Slow Queries**: Holding connections too long
3. **Traffic Spike**: More requests than pool can handle
4. **Pool Too Small**: Undersized for load

---

## Resolution

### Option 1: Increase Pool Size

```yaml
spring:
  datasource:
    hikaricp:
      maximum-pool-size: 50  # Increase from 20
      minimum-idle: 20
```

**Formula**: `pool_size = (core_count × 2) + effective_spindle_count`

### Option 2: Fix Connection Leaks

```java
// ❌ Bad - Connection leak
public void badMethod() {
    Connection conn = dataSource.getConnection();
    // ... use connection
    // Forgot to close!
}

// ✅ Good - Try-with-resources
public void goodMethod() {
    try (Connection conn = dataSource.getConnection()) {
        // ... use connection
    } // Auto-closed
}
```

### Option 3: Optimize Slow Queries

```sql
-- Find slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;

-- Add indexes
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```

---

## Verification

```bash
# Check pool utilization
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

# Should be < 80% of max
```

---

## Prevention

- Monitor pool utilization
- Set alerts at 80% utilization
- Regular query performance reviews
- Load testing before releases

---

**Last Updated**: 2026-01-19
