# Runbook: Database Connection Pool Exhaustion

**Severity**: High  
**Symptom**: `HikariPool - Connection is not available, request timed out after 30000ms`

---

## 1. Immediate Mitigation

1.  **Identify the affected service**: Check logs for the service throwing `HikariPool` errors.
2.  **Restart the service**: Often provides immediate relief by clearing stale connections.
    ```bash
    kubectl rollout restart deployment/<service-name>
    ```
3.  **Check DB Load**: If the DB is at 100% CPU, increasing connections may worsen the situation.

---

## 2. Diagnosis

### A. Check Active Connections
Connect to the database and check how many connections are active:
```sql
SELECT count(*), state 
FROM pg_stat_activity 
WHERE datname = 'your_db_name' 
GROUP BY state;
```

### B. Find Long Running Queries
```sql
SELECT pid, now() - query_start AS duration, query, state
FROM pg_stat_activity
WHERE state != 'idle' AND (now() - query_start) > interval '30 seconds'
ORDER BY duration DESC;
```

### C. Check for Connection Leaks
Check if the service has `hikaricp_connections_active` metric continuously increasing without dropping.

---

## 3. Resolution

### Increase Pool Size (Config Change)
If the DB can handle it, increase the max pool size:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
```

### Kill Hang Queries
```sql
SELECT pg_terminate_backend(pid);
```

---

## 4. Prevention
1.  **Enable Leak Detection**: `leak-detection-threshold: 60000`
2.  **Optimize Queries**: Add missing indexes.
3.  **Read Replicas**: Move read-only operations to a separate connection pool pointing to replicas.
