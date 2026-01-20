# 📖 Glossary

Domain terminology and technical concepts used in the E-commerce Backend.

---

## Business Domain Terms

### A

**Aggregate**  
**Definition**: A cluster of domain objects treated as a single unit for data changes.  
**Context**: In DDD, Order is an aggregate root containing OrderItems.  
**Related**: [Domain-Driven Design](architecture/system-overview.md)

**Audit Trail**  
**Definition**: Chronological record of system activities.  
**Context**: All entities have `created_at` and `updated_at` for audit purposes.  
**Related**: [Data Model](architecture/data-model.md)

### C

**Compensation**  
**Definition**: Reversal of a previously completed transaction.  
**Context**: When payment fails, we compensate by releasing reserved stock.  
**Related**: [Saga Pattern](architecture/order-saga-flow.md)

**Customer**  
**Definition**: End user who places orders.  
**Context**: Identified by `customerId`, has CUSTOMER role.  
**Related**: [RBAC](SECURITY.md#role-based-access-control-rbac)

### E

**Event**  
**Definition**: Immutable fact that something happened in the system.  
**Context**: `OrderCreated`, `PaymentCompleted` are domain events.  
**Related**: [Event Catalog](architecture/event-catalog.md)

**Event Sourcing** *(Partial)*  
**Definition**: Storing state changes as sequence of events.  
**Context**: We use events for communication, not full event sourcing.  
**Related**: [Kafka Events](architecture/event-catalog.md)

**Eventual Consistency**  
**Definition**: System will become consistent over time.  
**Context**: After `OrderCreated`, inventory becomes consistent eventually.  
**Related**: [System Overview](architecture/system-overview.md)

### I

**Idempotency**  
**Definition**: Operation can be applied multiple times without changing result.  
**Context**: Using `Idempotency-Key` header prevents duplicate orders.  
**Related**: [API Documentation](api/README.md#idempotency)

**Idempotency Key**  
**Definition**: Unique identifier to ensure request is processed only once.  
**Context**: Client-generated UUID sent in `Idempotency-Key` header.  
**Related**: [Configuration](CONFIGURATION.md)

**Inventory**  
**Definition**: Stock of products available for sale.  
**Context**: Tracks `availableQuantity` and `reservedQuantity`.  
**Related**: [Data Model](architecture/data-model.md#3-inventory-service-database-inventory_db)

### O

**Optimistic Locking**  
**Definition**: Concurrency control using version numbers.  
**Context**: Inventory uses `@Version` to prevent lost updates.  
**Related**: [Data Model](architecture/data-model.md#optimistic-locking)

**Order**  
**Definition**: Customer request to purchase products.  
**Context**: Aggregate containing order items, tracked by saga.  
**Related**: [Order Saga Flow](architecture/order-saga-flow.md)

**Orchestration**  
**Definition**: Central coordinator manages distributed transaction.  
**Context**: Order Service orchestrates the order creation saga.  
**Related**: [Saga Pattern](architecture/order-saga-flow.md)

**Outbox Pattern**  
**Definition**: Ensures reliable event publishing using database table.  
**Context**: Events saved to `outbox_events` table, then published to Kafka.  
**Related**: [Outbox Pattern](architecture/outbox-pattern.md)

### P

**Payment Gateway**  
**Definition**: External service that processes payments.  
**Context**: Payment Service integrates with gateway (simulated in dev).  
**Related**: [Payment Service](architecture/data-model.md#5-payment-service-database-payment_db)

**Product**  
**Definition**: Item available for purchase.  
**Context**: Has SKU, price, status (ACTIVE/DISCONTINUED).  
**Related**: [Data Model](architecture/data-model.md#2-product-service-database-product_db)

### R

**Reservation**  
**Definition**: Temporary hold on inventory for pending order.  
**Context**: Stock reserved when order created, released if cancelled.  
**Related**: [Inventory Events](architecture/event-catalog.md#inventory-events)

**RBAC (Role-Based Access Control)**  
**Definition**: Access control based on user roles.  
**Context**: CUSTOMER, ADMIN, SERVICE roles with different permissions.  
**Related**: [Security](SECURITY.md#role-based-access-control-rbac)

### S

**Saga**  
**Definition**: Pattern for managing distributed transactions.  
**Context**: Order creation uses orchestrated saga pattern.  
**Related**: [Order Saga Flow](architecture/order-saga-flow.md)

**Saga State**  
**Definition**: Current step in distributed transaction.  
**Context**: PENDING → INVENTORY_RESERVED → PAYMENT_COMPLETED → CONFIRMED.  
**Related**: [Order Saga Flow](architecture/order-saga-flow.md)

**SKU (Stock Keeping Unit)**  
**Definition**: Unique identifier for product.  
**Context**: Used for inventory tracking, must be unique.  
**Related**: [Product Service](architecture/data-model.md#2-product-service-database-product_db)

### T

**Transactional Outbox**  
**Definition**: See Outbox Pattern.  
**Context**: Guarantees at-least-once delivery of events.  
**Related**: [Outbox Pattern](architecture/outbox-pattern.md)

---

## Technical Terms

### A

**API Gateway**  
**Definition**: Single entry point for all client requests.  
**Context**: Handles authentication, rate limiting, routing.  
**Related**: [Security](SECURITY.md)

**Actuator**  
**Definition**: Spring Boot endpoints for monitoring and management.  
**Context**: `/actuator/health`, `/actuator/prometheus`.  
**Related**: [Observability](OBSERVABILITY.md)

### B

**Bcrypt**  
**Definition**: Password hashing algorithm.  
**Context**: Used with cost factor 12 for password storage.  
**Related**: [Security](SECURITY.md#sensitive-data-handling)

**Blue-Green Deployment**  
**Definition**: Deployment strategy with two identical environments.  
**Context**: Used for staging deployments.  
**Related**: [Deployment](DEPLOYMENT.md)

### C

**Canary Deployment**  
**Definition**: Gradual rollout to subset of users.  
**Context**: Production deployments start with 10% traffic.  
**Related**: [Deployment](DEPLOYMENT.md)

**Circuit Breaker**  
**Definition**: Prevents cascading failures by failing fast.  
**Context**: Resilience4j protects external service calls.  
**Related**: [System Overview](architecture/system-overview.md)

**CORS (Cross-Origin Resource Sharing)**  
**Definition**: Security mechanism for cross-domain requests.  
**Context**: Configured at API Gateway level.  
**Related**: [Security](SECURITY.md#cors-configuration)

**CQRS (Command Query Responsibility Segregation)**  
**Definition**: Separate models for reads and writes.  
**Context**: Lite implementation in some services.  
**Related**: [System Overview](architecture/system-overview.md)

### D

**DDD (Domain-Driven Design)**  
**Definition**: Software design approach focused on domain model.  
**Context**: Services organized around business domains.  
**Related**: [System Overview](architecture/system-overview.md)

**Dead Letter Queue (DLQ)**  
**Definition**: Queue for messages that failed processing.  
**Context**: Kafka messages moved to DLQ after max retries.  
**Related**: [Event Catalog](architecture/event-catalog.md)

**Distributed Tracing**  
**Definition**: Tracking requests across multiple services.  
**Context**: Zipkin with Trace ID and Span ID.  
**Related**: [Observability](OBSERVABILITY.md)

### F

**Flyway**  
**Definition**: Database migration tool.  
**Context**: Manages schema versions with `V1__`, `V2__` scripts.  
**Related**: [Development](DEVELOPMENT.md#database-migrations)

### H

**Hexagonal Architecture**  
**Definition**: Architecture pattern isolating core logic from infrastructure.  
**Context**: Domain layer has no framework dependencies.  
**Related**: [System Overview](architecture/system-overview.md)

**HikariCP**  
**Definition**: High-performance JDBC connection pool.  
**Context**: Default connection pool in Spring Boot.  
**Related**: [Configuration](CONFIGURATION.md#database-configuration)

### J

**JWT (JSON Web Token)**  
**Definition**: Compact token format for authentication.  
**Context**: Access tokens valid for 1 hour, refresh tokens for 7 days.  
**Related**: [Security](SECURITY.md#jwt-strategy)

### K

**Kafka**  
**Definition**: Distributed event streaming platform.  
**Context**: Used for async communication between services.  
**Related**: [Event Catalog](architecture/event-catalog.md)

**Kafka Consumer Lag**  
**Definition**: Difference between produced and consumed messages.  
**Context**: Monitored via Prometheus, alerted if > 1000.  
**Related**: [Observability](OBSERVABILITY.md#alerting-rules)

### M

**MDC (Mapped Diagnostic Context)**  
**Definition**: Thread-local storage for logging context.  
**Context**: Stores `traceId`, `spanId`, `userId` for logs.  
**Related**: [Observability](OBSERVABILITY.md#mdc-mapped-diagnostic-context)

**Microservices**  
**Definition**: Architectural style with independently deployable services.  
**Context**: 7 services: Gateway, Auth, Product, Inventory, Order, Payment, Notification.  
**Related**: [System Overview](architecture/system-overview.md)

### O

**OpenAPI**  
**Definition**: Specification for REST APIs.  
**Context**: Swagger UI generated from OpenAPI specs.  
**Related**: [API Documentation](api/README.md)

### P

**Polling Publisher**  
**Definition**: Scheduled task that publishes outbox events.  
**Context**: Polls `outbox_events` table every 1 second.  
**Related**: [Outbox Pattern](architecture/outbox-pattern.md)

**Prometheus**  
**Definition**: Time-series database for metrics.  
**Context**: Scrapes `/actuator/prometheus` every 15 seconds.  
**Related**: [Observability](OBSERVABILITY.md)

### R

**Rate Limiting**  
**Definition**: Restricting number of requests per time period.  
**Context**: 10 req/min for anonymous, 100 for authenticated.  
**Related**: [Security](SECURITY.md#rate-limiting)

**Redis**  
**Definition**: In-memory data store.  
**Context**: Used for caching and rate limiting.  
**Related**: [Configuration](CONFIGURATION.md#redis-configuration)

**Rolling Update**  
**Definition**: Gradual replacement of instances.  
**Context**: Default deployment strategy for development.  
**Related**: [Deployment](DEPLOYMENT.md)

### S

**SLI (Service Level Indicator)**  
**Definition**: Quantitative measure of service level.  
**Context**: Availability, latency, error rate.  
**Related**: [Observability](OBSERVABILITY.md#slislosla)

**SLO (Service Level Objective)**  
**Definition**: Target value for SLI.  
**Context**: 99.9% availability, P95 latency < 500ms.  
**Related**: [Observability](OBSERVABILITY.md#slislosla)

**SLA (Service Level Agreement)**  
**Definition**: Contract with customers about service levels.  
**Context**: API Gateway 99.9% uptime guarantee.  
**Related**: [Observability](OBSERVABILITY.md#slislosla)

**Span**  
**Definition**: Single unit of work in distributed trace.  
**Context**: Each service call creates a span with unique Span ID.  
**Related**: [Observability](OBSERVABILITY.md#distributed-tracing)

**SpringDoc**  
**Definition**: Library for OpenAPI documentation in Spring Boot.  
**Context**: Generates Swagger UI at `/swagger-ui.html`.  
**Related**: [API Documentation](api/README.md)

**STRIDE**  
**Definition**: Threat modeling framework (Spoofing, Tampering, Repudiation, Information Disclosure, DoS, Elevation of Privilege).  
**Context**: Used for security threat analysis.  
**Related**: [Security](SECURITY.md#stride-threat-analysis)

### T

**TLS (Transport Layer Security)**  
**Definition**: Cryptographic protocol for secure communication.  
**Context**: TLS 1.3 for all HTTPS connections.  
**Related**: [Security](SECURITY.md#encryption-in-transit)

**Trace ID**  
**Definition**: Unique identifier for entire request flow.  
**Context**: Propagated across services and Kafka.  
**Related**: [Observability](OBSERVABILITY.md#trace-context)

### U

**ULID (Universally Unique Lexicographically Sortable Identifier)**  
**Definition**: Time-ordered unique identifier.  
**Context**: Used for Order IDs, Product IDs (sortable by creation time).  
**Related**: [Data Model](architecture/data-model.md)

### V

**Virtual Threads**  
**Definition**: Lightweight threads in Java 21 (Project Loom).  
**Context**: Enabled in all services for better concurrency.  
**Related**: [Configuration](CONFIGURATION.md#virtual-threads-java-21)

### Z

**Zipkin**  
**Definition**: Distributed tracing system.  
**Context**: Visualizes request flow across services.  
**Related**: [Observability](OBSERVABILITY.md#zipkin-integration)

---

## Acronyms

| Acronym | Full Form | Definition |
|---------|-----------|------------|
| **ADR** | Architecture Decision Record | Document explaining architectural choice |
| **API** | Application Programming Interface | Contract for service communication |
| **CDC** | Change Data Capture | Tracking database changes |
| **CI/CD** | Continuous Integration/Continuous Deployment | Automated build and deployment |
| **DDD** | Domain-Driven Design | Software design approach |
| **DLQ** | Dead Letter Queue | Queue for failed messages |
| **ERD** | Entity Relationship Diagram | Database schema visualization |
| **GDPR** | General Data Protection Regulation | EU data privacy law |
| **HS256** | HMAC with SHA-256 | JWT signing algorithm |
| **HTTP** | Hypertext Transfer Protocol | Web communication protocol |
| **JDBC** | Java Database Connectivity | Java database API |
| **JPA** | Java Persistence API | ORM specification |
| **JSON** | JavaScript Object Notation | Data interchange format |
| **JWT** | JSON Web Token | Authentication token format |
| **MDC** | Mapped Diagnostic Context | Logging context storage |
| **MTTR** | Mean Time To Recovery | Average recovery time |
| **OWASP** | Open Web Application Security Project | Security standards org |
| **PCI DSS** | Payment Card Industry Data Security Standard | Payment security standard |
| **PII** | Personal Identifiable Information | Sensitive user data |
| **RBAC** | Role-Based Access Control | Permission system |
| **REST** | Representational State Transfer | API architectural style |
| **RFC** | Request for Comments | Internet standard |
| **SAST** | Static Application Security Testing | Code security analysis |
| **SKU** | Stock Keeping Unit | Product identifier |
| **SLA** | Service Level Agreement | Service guarantee |
| **SLI** | Service Level Indicator | Service metric |
| **SLO** | Service Level Objective | Service target |
| **SQL** | Structured Query Language | Database query language |
| **SRE** | Site Reliability Engineering | Operations discipline |
| **TLS** | Transport Layer Security | Encryption protocol |
| **ULID** | Universally Unique Lexicographically Sortable ID | Sortable unique ID |
| **UUID** | Universally Unique Identifier | Random unique ID |
| **YAML** | YAML Ain't Markup Language | Configuration format |

---

## Common Patterns

### Database per Service

**Definition**: Each microservice owns its database.  
**Context**: `product_db`, `order_db`, `inventory_db` are separate.  
**Related**: [Data Model](architecture/data-model.md)

### Event-Driven Architecture

**Definition**: Services communicate via events.  
**Context**: Kafka events for async communication.  
**Related**: [Event Catalog](architecture/event-catalog.md)

### Ports and Adapters

**Definition**: See Hexagonal Architecture.  
**Context**: Domain logic isolated from infrastructure.  
**Related**: [System Overview](architecture/system-overview.md)

---

## Next Steps

- [System Overview](architecture/system-overview.md) - Architecture context
- [Data Model](architecture/data-model.md) - Database schemas
- [Event Catalog](architecture/event-catalog.md) - Kafka events
- [API Documentation](api/README.md) - REST APIs

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-20  
**Maintained By**: Documentation Team
