# Runbook: Order Stuck Pending

**Severity**: High  
**Symptom**: Orders remain in `PENDING` state for > 5 minutes. No `OrderConfirmed` or `OrderCancelled` event generated.

---

## 1. Diagnosis

### A. Check Saga State
Query the `order_sagas` table in the Order DB:
```sql
SELECT order_id, state, last_error, updated_at 
FROM order_sagas 
WHERE state NOT IN ('CONFIRMED', 'CANCELLED') 
AND updated_at < NOW() - INTERVAL '5 minutes';
```

### B. Check Event Outbox
Check if the next required event was published but not sent:
```sql
SELECT * FROM outbox_events WHERE status = 'PENDING' AND aggregate_id = '<order-id>';
```

### C. Trace the Flow
Check logs for the specific `order-id` across Gateway, Order, Inventory, and Payment services.

---

## 2. Resolution

### A. Manual Event Re-Trigger
If the event is stuck in the outbox:
```sql
UPDATE outbox_events SET status = 'PENDING', published_at = NULL WHERE aggregate_id = '<order-id>';
```

### B. Force Cancellation
If the saga cannot be recovered:
```bash
curl -X POST http://api-gateway/api/v1/orders/<order-id>/cancel -H "Authorization: Bearer ..."
```

---

## 3. Prevention
1.  **Saga Timeout**: Implement a background job that automatically cancels orders stuck in a non-terminal state for > 30 minutes.
2.  **Alerting**: Alert when `COUNT(pending_sagas) > threshold`.
