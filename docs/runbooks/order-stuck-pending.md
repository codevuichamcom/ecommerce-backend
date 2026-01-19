# Runbook: Order Stuck in Pending Status

**Alert Name**: `OrderStuckPending`  
**Severity**: Critical  
**MTTR Target**: 10 minutes

---

## Alert Details

**Symptoms**:
- Order created but never moves to CONFIRMED/CANCELLED
- Customer complaints
- Saga state not progressing

---

## Diagnosis

### 1. Check Order Saga State

```sql
-- Connect to order_db
psql -h localhost -U postgres -d order_db

-- Check saga state
SELECT order_id, state, last_error, updated_at 
FROM order_sagas 
WHERE order_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H';
```

### 2. Check Outbox Events

```sql
-- Check if events published
SELECT * FROM outbox_events 
WHERE aggregate_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H'
ORDER BY created_at DESC;
```

### 3. Check Kafka Topics

```bash
# Check order events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  | grep "01HQZX3Y4Z5A6B7C8D9E0F1G2H"
```

---

## Resolution

### Option 1: Republish Events

```sql
-- Mark event as PENDING to retry
UPDATE outbox_events 
SET status = 'PENDING', published_at = NULL
WHERE aggregate_id = '01HQZX3Y4Z5A6B7C8D9E0F1G2H';
```

### Option 2: Manual Compensation

```bash
# Cancel stuck order
curl -X POST http://localhost:8083/api/v1/orders/01HQZX3Y4Z5A6B7C8D9E0F1G2H/cancel \
  -H "Authorization: Bearer TOKEN"
```

### Option 3: Check Downstream Services

```bash
# Check inventory service
curl http://localhost:8082/actuator/health

# Check payment service
curl http://localhost:8084/actuator/health
```

---

## Prevention

- Monitor saga state transitions
- Alert on orders pending > 5 minutes
- Implement saga timeout mechanism

---

**Last Updated**: 2026-01-19
