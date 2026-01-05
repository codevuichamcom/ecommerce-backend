# Phase 1: Implementation Details

## Services Overview

### 1. Product Service (Port 8081)

**Responsibilities:**
- Product catalog management
- CRUD operations

**Key Files:**
| Layer | Path |
|-------|------|
| Domain | `domain/model/Product.java` |
| Application | `application/service/ProductService.java` |
| Infrastructure | `infrastructure/web/ProductController.java` |

**API Endpoints:**
```
POST   /api/products          - Create product
GET    /api/products/{id}     - Get product
GET    /api/products          - List all
PUT    /api/products/{id}     - Update product
DELETE /api/products/{id}     - Delete product
POST   /api/products/{id}/activate    - Activate
POST   /api/products/{id}/deactivate  - Deactivate
```

---

### 2. Inventory Service (Port 8082)

**Responsibilities:**
- Stock quantity tracking
- Reservation for orders
- Concurrency handling

**Key Features:**
- `@Version` for optimistic locking
- `@Retryable` for automatic retry (3 attempts, exponential backoff)
- Sealed `StockOperationResult` for exhaustive pattern matching

**API Endpoints:**
```
POST   /api/inventory              - Create inventory
GET    /api/inventory/product/{id} - Get by product
POST   /api/inventory/reserve      - Reserve stock
POST   /api/inventory/release      - Release stock
POST   /api/inventory/product/{id}/add - Add stock
```

---

### 3. Order Service (Port 8083)

**Responsibilities:**
- Order lifecycle management
- Cross-service coordination
- Idempotent operations

**Key Features:**
- `Idempotency-Key` header support
- Automatic inventory rollback on failure
- Sealed `OrderStatus` for state machine

**Order Flow:**
```
1. Check idempotency key
2. Validate products (call product-service)
3. Reserve inventory (call inventory-service)
4. Create order → PENDING
5. Confirm order → CONFIRMED
6. On failure → rollback reservations
```

**API Endpoints:**
```
POST   /api/orders              - Create order
GET    /api/orders/{id}         - Get order
GET    /api/orders?customerId=  - List by customer
POST   /api/orders/{id}/cancel  - Cancel order
```

---

## Java 21 Patterns Used

### Sealed Classes

```java
// BusinessException hierarchy
public sealed class BusinessException extends RuntimeException
    permits NotFoundException, ConflictException, ValidationException { }

// Order status
public sealed interface OrderStatus {
    record Pending() implements OrderStatus { }
    record Confirmed() implements OrderStatus { }
    record Cancelled(String reason) implements OrderStatus { }
}

// Stock operation results
public sealed interface StockOperationResult {
    record Success(...) implements StockOperationResult { }
    record InsufficientStock(...) implements StockOperationResult { }
}
```

### Pattern Matching

```java
// Exception handling
HttpStatus status = switch (ex) {
    case NotFoundException _ -> HttpStatus.NOT_FOUND;
    case ConflictException _ -> HttpStatus.CONFLICT;
    case ValidationException _ -> HttpStatus.BAD_REQUEST;
};

// Stock result handling
return switch (result) {
    case Success s -> StockOperationResponse.success(...);
    case InsufficientStock is -> StockOperationResponse.failure(...);
};
```

### Records

```java
// Value objects
public record ProductId(String value) implements ValueObject { }
public record Money(BigDecimal amount, String currency) { }

// Commands
public record CreateProductCommand(
    @NotBlank String name,
    @NotNull BigDecimal price
) { }

// Domain events
public record OrderCreated(
    String eventId,
    Instant occurredAt,
    String orderId
) implements DomainEvent { }
```

---

## Database Schemas

### Products (product_db)
```sql
CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);
```

### Inventory (inventory_db)
```sql
CREATE TABLE inventory (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL UNIQUE,
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL,
    version BIGINT NOT NULL  -- Optimistic locking
);
```

### Orders (order_db)
```sql
CREATE TABLE orders (
    id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    total_amount DECIMAL(19, 2) NOT NULL
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) REFERENCES orders(id),
    product_id VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL
);
```
