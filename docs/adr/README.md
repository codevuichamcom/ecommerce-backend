# Architecture Decision Records

This directory contains Architecture Decision Records (ADRs) documenting significant architectural decisions made in the E-commerce Backend project.

---

## What is an ADR?

An **Architecture Decision Record** (ADR) captures an important architectural decision along with its context and consequences.

### When to Create an ADR

Create an ADR when making decisions about:
- Technology choices (frameworks, databases, messaging systems)
- Architectural patterns (microservices, event-driven, hexagonal)
- Cross-cutting concerns (security, observability, deployment)
- Significant trade-offs

### ADR Format

Each ADR follows this structure:
1. **Title**: Short noun phrase
2. **Status**: Proposed, Accepted, Deprecated, Superseded
3. **Context**: What is the issue we're facing?
4. **Decision**: What did we decide?
5. **Consequences**: What are the results (positive and negative)?

---

## ADR Index

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [001](001-hexagonal-architecture.md) | Hexagonal Architecture Pattern | Accepted | 2026-01-19 |
| [002](002-kafka-over-rabbitmq.md) | Kafka over RabbitMQ for Event Streaming | Accepted | 2026-01-19 |
| [003](003-saga-orchestration-pattern.md) | Saga Orchestration Pattern for Distributed Transactions | Accepted | 2026-01-19 |
| [004](004-postgresql-per-service.md) | PostgreSQL Database per Service | Accepted | 2026-01-19 |
| [005](005-ulid-over-uuid.md) | ULID over UUID for Entity IDs | Accepted | 2026-01-19 |

---

## Creating a New ADR

1. Copy the [template](000-template.md)
2. Number it sequentially (e.g., `006-my-decision.md`)
3. Fill in all sections
4. Submit for review via Pull Request
5. Update this index

---

## ADR Lifecycle

```
Proposed → Accepted → [Deprecated/Superseded]
```

- **Proposed**: Under discussion
- **Accepted**: Decision made and implemented
- **Deprecated**: No longer recommended but still in use
- **Superseded**: Replaced by a newer ADR

---

## Related Resources

- [ADR GitHub Organization](https://adr.github.io/)
- [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [Architecture Decision Records (ThoughtWorks)](https://www.thoughtworks.com/radar/techniques/lightweight-architecture-decision-records)
