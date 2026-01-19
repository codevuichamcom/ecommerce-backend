# ADR-001: Hexagonal Architecture Pattern

**Status**: Accepted  
**Date**: 2026-01-15  
**Deciders**: Solution Architect, Tech Lead, Senior Developers  
**Technical Story**: Initial architecture design

---

## Context

We needed to choose an architectural pattern for organizing code within each microservice that would:
- Separate business logic from infrastructure concerns
- Make the codebase testable and maintainable
- Allow easy replacement of infrastructure components (databases, messaging systems)
- Support Domain-Driven Design principles

### Problem Statement

Traditional layered architecture often leads to:
- Business logic leaking into infrastructure layers
- Tight coupling to frameworks and databases
- Difficulty in testing business logic in isolation
- Framework-driven design instead of domain-driven design

### Constraints

- Must work with Spring Boot framework
- Team has varying levels of DDD experience
- Need to balance purity with pragmatism

### Assumptions

- Business logic is complex enough to warrant separation
- Team can learn and adopt the pattern
- Benefits outweigh the initial learning curve

---

## Decision

We will adopt **Hexagonal Architecture** (also known as Ports and Adapters) for organizing code within each microservice.

**Core Principles**:
1. **Domain Layer**: Pure business logic, no framework dependencies
2. **Application Layer**: Use cases and orchestration
3. **Infrastructure Layer**: Adapters for databases, messaging, HTTP

---

## Considered Options

### Option 1: Traditional Layered Architecture

**Pros**:
- Familiar to most developers
- Simple to understand
- Well-supported by Spring Boot

**Cons**:
- Business logic often leaks into other layers
- Tight coupling to infrastructure
- Difficult to test in isolation
- Framework-driven design

### Option 2: Hexagonal Architecture (Ports and Adapters)

**Pros**:
- Clear separation of concerns
- Business logic independent of infrastructure
- Highly testable
- Easy to swap infrastructure components
- Supports DDD principles

**Cons**:
- Steeper learning curve
- More boilerplate code
- Requires discipline to maintain boundaries

### Option 3: Clean Architecture

**Pros**:
- Similar benefits to Hexagonal
- Well-documented by Uncle Bob
- Clear dependency rules

**Cons**:
- More layers than Hexagonal
- Can be overly complex for our needs
- Similar learning curve

---

## Decision Outcome

**Chosen Option**: Hexagonal Architecture

**Justification**:
- Provides the right balance of separation and simplicity
- Aligns well with DDD principles we want to adopt
- Makes business logic highly testable
- Allows us to evolve infrastructure independently
- Well-suited for microservices architecture

---

## Consequences

### Positive

- **Testability**: Business logic can be tested without infrastructure
- **Flexibility**: Easy to swap databases, messaging systems
- **Maintainability**: Clear boundaries reduce coupling
- **Domain Focus**: Encourages domain-driven design
- **Technology Independence**: Domain layer has no framework dependencies

### Negative

- **Learning Curve**: Team needs training on the pattern
- **Boilerplate**: More interfaces and adapters
- **Initial Complexity**: Takes longer to set up initially
- **Discipline Required**: Easy to violate boundaries if not careful

### Neutral

- **Package Structure**: Requires thoughtful organization
- **Testing Strategy**: Need both unit and integration tests

---

## Implementation

### Package Structure

```
com.ecommerce.{service}/
├── domain/
│   ├── model/           # Domain entities, value objects
│   ├── port/            # Ports (interfaces)
│   │   ├── in/          # Inbound ports (use cases)
│   │   └── out/         # Outbound ports (repositories, etc.)
│   └── service/         # Domain services
├── application/
│   └── service/         # Application services (use case implementations)
└── infrastructure/
    ├── persistence/     # Database adapters
    ├── messaging/       # Kafka adapters
    └── web/             # HTTP adapters (controllers)
```

### Example: Product Service

**Domain Layer** (no dependencies):
```java
// domain/model/Product.java
public class Product {
    private final ProductId id;
    private String name;
    private Money price;
    
    public void updatePrice(Money newPrice) {
        // Business logic
    }
}

// domain/port/in/CreateProductUseCase.java
public interface CreateProductUseCase {
    ProductResponse execute(CreateProductCommand command);
}

// domain/port/out/ProductRepository.java
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
}
```

**Application Layer**:
```java
// application/service/ProductService.java
@Service
public class ProductService implements CreateProductUseCase {
    private final ProductRepository productRepository;
    
    @Override
    public ProductResponse execute(CreateProductCommand command) {
        Product product = Product.create(command.name(), command.price());
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }
}
```

**Infrastructure Layer**:
```java
// infrastructure/persistence/ProductJpaAdapter.java
@Repository
public class ProductJpaAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    
    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = ProductJpaEntity.from(product);
        ProductJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
}

// infrastructure/web/ProductController.java
@RestController
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    
    @PostMapping("/api/products")
    public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
        CreateProductCommand command = CreateProductCommand.from(request);
        ProductResponse response = createProductUseCase.execute(command);
        return ResponseEntity.status(201).body(response);
    }
}
```

### Migration Strategy

1. Start with new services using Hexagonal Architecture
2. Gradually refactor existing services
3. Focus on domain layer first, then adapters
4. Maintain backward compatibility during migration

---

## Validation

### Success Criteria

- ✅ Domain layer has zero framework dependencies
- ✅ Business logic testable without infrastructure
- ✅ Can swap database implementation without changing domain
- ✅ Clear separation between layers

### Metrics

- **Test Coverage**: Domain layer > 90%
- **Coupling**: Domain layer has no outbound dependencies
- **Build Time**: Domain layer builds independently

---

## Related Decisions

- [ADR-002: Kafka over RabbitMQ](002-kafka-over-rabbitmq.md) - Infrastructure choice
- [ADR-004: PostgreSQL per Service](004-postgresql-per-service.md) - Database strategy

---

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Get Your Hands Dirty on Clean Architecture](https://www.packtpub.com/product/get-your-hands-dirty-on-clean-architecture/9781839211966)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

---

**Last Updated**: 2026-01-19
