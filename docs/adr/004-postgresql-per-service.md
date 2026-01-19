# ADR-004: PostgreSQL Database per Service

**Status**: Accepted  
**Date**: 2026-01-15  
**Deciders**: Solution Architect, DBA, Tech Lead  
**Technical Story**: Database architecture design

---

## Context

We needed to decide on database strategy for microservices: shared database vs database per service.

### Problem Statement

How should microservices store and access data while maintaining:
- Service independence
- Data consistency
- Performance
- Operational simplicity

---

## Decision

We will use **PostgreSQL with Database per Service** pattern.

---

## Considered Options

### Option 1: Shared Database

**Pros**: Simple, ACID transactions, no data duplication  
**Cons**: Tight coupling, schema changes affect all services, scaling bottleneck

### Option 2: Database per Service

**Pros**: Service independence, technology flexibility, easier scaling  
**Cons**: Distributed transactions, data duplication, operational complexity

### Option 3: Shared Database with Schemas

**Pros**: Logical separation, single database instance  
**Cons**: Still coupled, schema changes risky

---

## Decision Outcome

**Chosen Option**: Database per Service with PostgreSQL

**Justification**: 
- Service independence is critical for microservices
- PostgreSQL provides excellent performance and features
- Operational complexity manageable with modern tools

---

## Consequences

### Positive
- Services can evolve independently
- No schema lock-in
- Easier to scale individual services
- Clear ownership boundaries

### Negative
- No cross-database joins
- Data duplication required
- More databases to manage
- Distributed transactions needed

---

## Implementation

**Databases**:
- `auth_db` - Auth Service
- `product_db` - Product Service
- `inventory_db` - Inventory Service
- `order_db` - Order Service
- `payment_db` - Payment Service
- `notification_db` - Notification Service

**Connection Pools**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
```

**Migrations**: Flyway per service

---

## Related Decisions

- [ADR-001: Hexagonal Architecture](001-hexagonal-architecture.md)
- [ADR-002: Kafka over RabbitMQ](002-kafka-over-rabbitmq.md)

---

## References

- [Database per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Last Updated**: 2026-01-19
