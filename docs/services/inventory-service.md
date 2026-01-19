# Inventory Service

**Port**: 8082
**Database**: `inventory_db`
**Technology**: Spring Boot, PostgreSQL, Kafka
**Pattern**: Domain-Driven Design (DDD) with Optimistic Locking

---

## Overview

The Inventory Service manages stock levels for products. It supports real-time stock reservations for orders and handles replenishment. It is a critical component of the Order Saga.

### Key Responsibilities

- **Stock Management**: Track available and reserved stock.
- **Reservations**: Atomic reservation of stock for orders.
- **Saga Participation**: Coordinates with Order Service via Kafka.
- **Concurrency Control**: Uses optimistic locking to handle high-frequency stock updates.

---

## API Endpoints

### Queries

#### Get Inventory by Product

```http
GET /api/inventory/product/{productId}
```

### Commands (Secured)

#### Initialize Inventory

```http
POST /api/inventory
Content-Type: application/json

{
  "productId": "prd-123...",
  "quantity": 100
}
```

#### Add Stock (Replenishment)

```http
POST /api/inventory/product/{productId}/add?quantity=50
```

#### Manual Reservation/Release (Debugging)

- **Reserve**: `POST /api/inventory/reserve`
- **Release**: `POST /api/inventory/release`

---

## Kafka Integration

Implemented in `InventoryEventConsumer.java`.

### Consumed Events (`order-events`)

| Event Type | Action |
|------------|--------|
| `OrderCreated` | Attempts to reserve stock for all items in the order. |
| `OrderCancelled`| Releases previously reserved stock. |

### Produced Events (`inventory-events`)

| Event Type | Trigger | Description |
|------------|---------|-------------|
| `StockReserved` | Partial Success | Published when an individual item is reserved. |
| `StockReservationFailed` | Failure | Published if an item is out of stock. |
| `AllItemsReserved` | Total Success | Published when all items in an order are secured. |

---

## Domain Model

### Inventory Aggregate

Manages `availableQuantity` and `reservedQuantity`.

**Logic**:
- **Reserve**: `available -= qty`, `reserved += qty`.
- **Release**: `reserved -= qty`, `available += qty`.
- **Confirm**: `reserved -= qty` (sold).

### Optimistic Locking

The service uses a `@Version` field to prevent lost updates when multiple orders attempt to reserve stock for the same product simultaneously. It includes a retry mechanism for `ObjectOptimisticLockingFailureException`.

---

## Database Schema

```sql
CREATE TABLE inventory (
    id VARCHAR(100) PRIMARY KEY,
    product_id VARCHAR(100) UNIQUE NOT NULL,
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

---

## Running

```bash
# Run locally
./gradlew :inventory-service:bootRun

# Docker
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/inventory_db \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  ecommerce/inventory-service
```
