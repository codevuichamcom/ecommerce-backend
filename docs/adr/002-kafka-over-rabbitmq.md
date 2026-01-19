# ADR-002: Kafka over RabbitMQ for Event Streaming

**Status**: Accepted  
**Date**: 2026-01-15  
**Deciders**: Solution Architect, Tech Lead, DevOps Lead  
**Technical Story**: Event-driven architecture design

---

## Context

We needed to choose a message broker for asynchronous communication between microservices that would support:
- Event streaming and event sourcing patterns
- High throughput (thousands of messages per second)
- Message persistence and replay capability
- Scalability for future growth

### Problem Statement

Our microservices need to communicate asynchronously for:
- Order saga orchestration
- Inventory updates
- Payment processing
- Notifications

Traditional request/response patterns would create tight coupling and reduce system resilience.

### Constraints

- Must handle peak loads of 10,000+ messages/second
- Messages must be persisted for audit and replay
- Must support multiple consumers for the same message
- Team has limited experience with message brokers

### Assumptions

- Event-driven architecture is the right choice
- We need message persistence beyond typical queue TTL
- System will grow 10x in next 2 years

---

## Decision

We will use **Apache Kafka** as our primary message broker for event streaming.

---

## Considered Options

### Option 1: RabbitMQ

**Pros**:
- Mature and widely adopted
- Excellent documentation
- Supports multiple messaging patterns (pub/sub, routing, RPC)
- Easier to learn and operate
- Good management UI

**Cons**:
- Lower throughput compared to Kafka
- Messages deleted after consumption
- Limited replay capability
- Horizontal scaling is complex
- Not designed for event streaming

**Throughput**: ~20,000 messages/second per broker

### Option 2: Apache Kafka

**Pros**:
- Designed for high-throughput event streaming
- Messages persisted to disk (configurable retention)
- Built-in message replay capability
- Excellent horizontal scalability
- Strong ordering guarantees per partition
- Supports event sourcing patterns
- Large ecosystem (Kafka Streams, Connect)

**Cons**:
- Steeper learning curve
- More complex to operate
- Requires ZooKeeper (until KRaft mode is stable)
- Overkill for simple queuing

**Throughput**: ~1,000,000 messages/second per cluster

### Option 3: AWS SQS/SNS

**Pros**:
- Fully managed (no operations)
- Pay-per-use pricing
- Integrates well with AWS services
- Simple to use

**Cons**:
- Vendor lock-in
- Limited message retention (14 days max)
- No message replay
- Higher latency
- Limited ordering guarantees

---

## Decision Outcome

**Chosen Option**: Apache Kafka

**Justification**:
1. **Throughput**: Can handle 50x our current peak load
2. **Persistence**: Messages retained for 7 days (configurable)
3. **Replay**: Critical for debugging and reprocessing
4. **Scalability**: Linear scaling by adding brokers
5. **Event Sourcing**: Aligns with our architectural patterns
6. **Future-proof**: Supports advanced patterns (CQRS, event sourcing)

---

## Consequences

### Positive

- **High Throughput**: Can handle millions of messages per day
- **Durability**: Messages persisted to disk, survives broker restarts
- **Replay Capability**: Can reprocess events from any point in time
- **Scalability**: Add brokers to scale horizontally
- **Ordering**: Strong ordering guarantees within partitions
- **Multiple Consumers**: Multiple consumer groups can read same events
- **Ecosystem**: Rich ecosystem (Kafka Streams, Connect, Schema Registry)

### Negative

- **Complexity**: More complex to set up and operate than RabbitMQ
- **Learning Curve**: Team needs training on Kafka concepts
- **Operational Overhead**: Requires monitoring and tuning
- **Resource Usage**: Higher memory and disk requirements
- **ZooKeeper Dependency**: Adds another component to manage

### Neutral

- **Message Size**: 1MB default limit (configurable)
- **Latency**: Slightly higher than RabbitMQ for low-volume scenarios
- **Tooling**: Requires additional tools for management (Kafka UI)

---

## Implementation

### Kafka Configuration

**Development**:
```yaml
# docker-compose.yml
kafka:
  image: confluentinc/cp-kafka:7.5.0
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

**Production**:
- 5 brokers for high availability
- Replication factor: 3
- Min in-sync replicas: 2
- Retention: 7 days

### Topics

| Topic | Partitions | Replication | Retention |
|-------|------------|-------------|-----------|
| `order-events` | 10 | 3 | 7 days |
| `inventory-events` | 10 | 3 | 7 days |
| `payment-events` | 10 | 3 | 30 days |
| `notification-events` | 5 | 3 | 3 days |

### Producer Configuration

```yaml
spring:
  kafka:
    producer:
      acks: all                    # Wait for all replicas
      retries: 3                   # Retry on failure
      compression-type: snappy     # Compress messages
      properties:
        enable.idempotence: true   # Exactly-once semantics
```

### Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      group-id: ${spring.application.name}-group
      auto-offset-reset: earliest
      enable-auto-commit: false    # Manual commit for reliability
```

### Migration Strategy

1. **Phase 1**: Set up Kafka cluster (development)
2. **Phase 2**: Implement Outbox pattern for reliable publishing
3. **Phase 3**: Migrate one service at a time
4. **Phase 4**: Production deployment with monitoring

---

## Validation

### Success Criteria

- ✅ Handle 10,000 messages/second with < 100ms latency
- ✅ Zero message loss during broker failures
- ✅ Consumer lag < 1000 messages under normal load
- ✅ Messages retained for 7 days minimum

### Metrics

**Throughput**:
```promql
rate(kafka_producer_record_send_total[5m])
```

**Consumer Lag**:
```promql
kafka_consumer_lag{topic="order-events"}
```

**Latency**:
```promql
histogram_quantile(0.95, rate(kafka_producer_record_send_latency_bucket[5m]))
```

---

## Related Decisions

- [ADR-003: Saga Orchestration Pattern](003-saga-orchestration-pattern.md) - Uses Kafka for saga coordination
- [ADR-001: Hexagonal Architecture](001-hexagonal-architecture.md) - Kafka as infrastructure adapter

---

## References

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka: The Definitive Guide](https://www.confluent.io/resources/kafka-the-definitive-guide/)
- [Designing Event-Driven Systems](https://www.confluent.io/designing-event-driven-systems/)
- [Kafka vs RabbitMQ Comparison](https://www.confluent.io/kafka-vs-rabbitmq/)

---

**Last Updated**: 2026-01-19
