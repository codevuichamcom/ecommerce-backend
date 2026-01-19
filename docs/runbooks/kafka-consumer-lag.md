# Runbook: Kafka Consumer Lag

**Severity**: Medium/High  
**Symptom**: `order-events` or `payment-events` have high lag; business operations (e.g., confirmations) are delayed.

---

## 1. Immediate Mitigation

1.  **Scale Consumers**: Increase the number of pods for the consumer service.
    ```bash
    kubectl scale deployment/<service-name> --replicas=5
    ```
2.  **Verify Kafka Health**: Ensure brokers are not under heavy load or disk pressure.

---

## 2. Diagnosis

### A. Check Lag Statistics
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --group <group-id> --describe
```

### B. Check Consumer Logs
Look for:
- `Slow processing of event ID: ...`
- `Request timeout`
- `Rebalancing group` (Too many rebalances prevent processing)

### C. Identify Blocking I/O
Is the consumer waiting on a slow DB query or an external API? Check distributed traces in Zipkin.

---

## 3. Resolution

### Increase Concurrency
If the topic has multiple partitions, increase service concurrency:
```yaml
spring:
  kafka:
    listener:
      concurrency: 3
```

### Skip Poison Pill Message (Last Resort)
If a single malformed message is crashing the consumer:
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --group <group-id> --topic <topic-name> --reset-offsets --to-offset <next-offset> --execute
```

---

## 4. Prevention
1.  **Async Processing**: Use a thread pool for non-transactional work.
2.  **Batching**: Enable `max-poll-records` to process events in batches.
3.  **Dead Letter Queue (DLQ)**: Ensure failing messages are moved to a DLQ after N retries instead of blocking the main pipe.
