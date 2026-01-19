# Runbook: Kafka Consumer Lag

**Alert Name**: `KafkaConsumerLag`  
**Severity**: Warning  
**MTTR Target**: 15 minutes

---

## Alert Details

**Trigger Condition**:
```promql
kafka_consumer_lag > 1000
```

**Symptoms**:
- Consumer lag increasing
- Events processed slowly
- Delayed notifications/updates

---

## Diagnosis

### 1. Check Current Lag

```bash
# View consumer groups
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Check specific group lag
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group order-service-group \
  --describe
```

**Expected Output**:
```
TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
order-events    0          1000            1500            500
order-events    1          2000            2100            100
```

### 2. Check Consumer Status

```bash
# Check if consumers are running
kubectl get pods -l app=order-service

# Check consumer logs
kubectl logs -f order-service-xxx | grep "Kafka"
```

### 3. Check Producer Rate

```bash
# Prometheus query
rate(kafka_producer_record_send_total[5m])
```

---

## Resolution

### Option 1: Increase Consumer Concurrency

**When**: Lag is consistent, consumers healthy

```yaml
# application.yml
spring:
  kafka:
    listener:
      concurrency: 5  # Increase from 3
```

**Steps**:
1. Update configuration
2. Restart service
3. Monitor lag reduction

### Option 2: Scale Horizontally

**When**: Single consumer at capacity

```bash
# Scale up instances
kubectl scale deployment order-service --replicas=3
```

### Option 3: Optimize Consumer Logic

**When**: Consumers slow due to processing

**Check**:
- Slow database queries
- External API calls
- Heavy computation

**Fix**:
- Add database indexes
- Use async processing
- Batch operations

### Option 4: Increase Partitions

**When**: Partitions < consumer instances

```bash
# Add partitions (cannot be undone!)
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --alter \
  --topic order-events \
  --partitions 10
```

---

## Verification

```bash
# Check lag is decreasing
watch -n 5 'docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group order-service-group \
  --describe'
```

**Success Criteria**:
- Lag < 1000 messages
- Lag decreasing consistently

---

## Escalation

**Escalate if**:
- Lag continues growing after 30 minutes
- Consumers crashing repeatedly
- Kafka cluster issues

**Contact**: On-call SRE via PagerDuty

---

## Prevention

- Monitor consumer lag continuously
- Set up alerts at lag > 500
- Regular capacity planning
- Load testing before releases

---

**Last Updated**: 2026-01-19
