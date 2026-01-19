# ADR-003: Saga Orchestration Pattern for Distributed Transactions

**Status**: Accepted  
**Date**: 2026-01-15  
**Deciders**: Solution Architect, Tech Lead  
**Technical Story**: Order processing workflow design

---

## Context

In a microservices architecture, we need to handle distributed transactions that span multiple services (Order, Inventory, Payment). Traditional ACID transactions don't work across service boundaries.

### Problem Statement

Creating an order requires:
1. Creating order record (Order Service)
2. Reserving inventory (Inventory Service)
3. Processing payment (Payment Service)

If any step fails, we need to rollback previous steps (compensating transactions).

---

## Decision

We will use **Saga Orchestration Pattern** with Order Service as the orchestrator.

---

## Considered Options

### Option 1: Saga Choreography

**Pros**: Decentralized, no single point of failure  
**Cons**: Complex to understand, hard to debug, no central view

### Option 2: Saga Orchestration

**Pros**: Central control, easy to debug, clear workflow  
**Cons**: Orchestrator is single point of failure

### Option 3: Two-Phase Commit (2PC)

**Pros**: Strong consistency  
**Cons**: Blocking, poor performance, not suitable for microservices

---

## Decision Outcome

**Chosen Option**: Saga Orchestration

**Justification**: Provides clear visibility and control over the order workflow, easier to debug and monitor.

---

## Consequences

### Positive
- Clear workflow visibility
- Easy to add new steps
- Centralized error handling
- Better monitoring

### Negative
- Orchestrator is critical component
- More complex than simple event chains
- Requires saga state management

---

## Implementation

**Saga States**:
```
PENDING → INVENTORY_RESERVED → PAYMENT_COMPLETED → CONFIRMED
                ↓                      ↓
            CANCELLED ← ─ ─ ─ ─ ─ ─ ─ ┘
```

**Saga State Table**:
```sql
CREATE TABLE order_sagas (
    order_id VARCHAR(26) PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    last_error TEXT,
    version INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## Related Decisions

- [ADR-002: Kafka over RabbitMQ](002-kafka-over-rabbitmq.md)
- [ADR-001: Hexagonal Architecture](001-hexagonal-architecture.md)

---

## References

- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Implementing Sagas](https://www.youtube.com/watch?v=xDuwrtwYHu8)

---

**Last Updated**: 2026-01-19
