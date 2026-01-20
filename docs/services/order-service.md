# Order Service

**Port**: 8083  
**Database**: `order_db`  
**Kafka**: Producer & Consumer  
**Swagger UI**: http://localhost:8083/swagger-ui.html

---

## Overview

Order Service is the core orchestrator for order processing, implementing the Saga pattern to coordinate distributed transactions across Inventory and Payment services.

### Responsibilities

- Create and manage orders
- Orchestrate order saga (inventory → payment)
- Handle order cancellations
- Provide order history

### Technology Stack

- **Framework**: Spring Boot 3.2
- **Language**: Java 21 (Virtual Threads)
- **Database**: PostgreSQL 16
- **Messaging**: Apache Kafka
- **Cache**: Redis

---

## API Endpoints

### Create Order

```http
POST /api/v1/orders
Authorization: Bearer {token}
Idempotency-Key: {uuid}
Content-Type: application/json

{
  "customerId": "customer-123",
  "items": [
    {
      "productId": "01HQZY5Z6A7B8C9D0E1F2G3H4I",
      "quantity": 2
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
  "customerId": "customer-123",
  "status": "PENDING",
  "items": [...],
  "totalAmount": 2599.98,
  "currency": "USD",
  "createdAt": "2026-01-19T10:15:30Z"
}
```

### Get Order

```http
GET /api/v1/orders/{id}
Authorization: Bearer {token}
```

### List Customer Orders

```http
GET /api/v1/orders/customer/{customerId}?page=0&size=20
Authorization: Bearer {token}
```

### Cancel Order

```http
POST /api/v1/orders/{id}/cancel
Authorization: Bearer {token}
```

---

## Database Schema

### Tables

**orders**:
```sql
CREATE TABLE orders (
    id VARCHAR(26) PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    cancel_reason TEXT,
    total_amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE UNIQUE INDEX idx_orders_idempotency_key ON orders(idempotency_key);
```

**order_items**:
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(26) NOT NULL REFERENCES orders(id),
    product_id VARCHAR(26) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
```

**order_sagas**:
```sql
CREATE TABLE order_sagas (
    order_id VARCHAR(26) PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);
```

**outbox_events** & **processed_events**: See [Outbox Pattern](../architecture/outbox-pattern.md)

---

## Kafka Events

### Produced Events

| Event | Topic | Description |
|-------|-------|-------------|
| `OrderCreated` | order-events | Order created, triggers inventory reservation |
| `OrderConfirmed` | order-events | Order completed successfully |
| `OrderCancelled` | order-events | Order cancelled, triggers compensation |
| `PaymentRequested` | payment-events | Request payment processing |

### Consumed Events

| Event | Topic | Action |
|-------|-------|--------|
| `AllItemsReserved` | inventory-events | Proceed to payment |
| `StockReservationFailed` | inventory-events | Cancel order |
| `PaymentCompleted` | payment-events | Confirm order |
| `PaymentFailed` | payment-events | Cancel order, release stock |

---

## Saga Flow

```
PENDING → INVENTORY_RESERVED → PAYMENT_COMPLETED → CONFIRMED
             ↓                        ↓
         CANCELLED ← ─ ─ ─ ─ ─ ─ ─ ─ ┘
```

See [Order Saga Flow](../architecture/order-saga-flow.md) for details.

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/order_db | Database URL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | localhost:9092 | Kafka brokers |
| `PRODUCT_SERVICE_URL` | http://localhost:8081 | Product Service URL |
| `INVENTORY_SERVICE_URL` | http://localhost:8082 | Inventory Service URL |

### Application Properties

```yaml
server:
  port: 8083

spring:
  application:
    name: order-service
  datasource:
    hikari:
      maximum-pool-size: 20
  kafka:
    consumer:
      group-id: order-service-group
    listener:
      concurrency: 3

services:
  product:
    url: ${PRODUCT_SERVICE_URL:http://localhost:8081}
  inventory:
    url: ${INVENTORY_SERVICE_URL:http://localhost:8082}
```

---

## Deployment

### Docker

```bash
# Build
./gradlew :order-service:bootJar

# Run
docker run -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/order_db \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  ecommerce/order-service:latest
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    spec:
      containers:
      - name: order-service
        image: ecommerce/order-service:1.0.0
        ports:
        - containerPort: 8083
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/order_db
```

---

## Monitoring

### Key Metrics

```promql
# Request rate
rate(http_server_requests_seconds_count{uri="/api/v1/orders"}[5m])

# Order creation rate
rate(orders_created_total[5m])

# Order completion rate
rate(orders_completed_total[5m]) / rate(orders_created_total[5m])

# Saga success rate
rate(orders_completed_total[5m]) / (rate(orders_completed_total[5m]) + rate(orders_cancelled_total[5m]))
```

### Alerts

- Order creation latency > 2s
- Order completion rate < 95%
- Saga failures > 5%

---

## Troubleshooting

### Orders Stuck in PENDING

See [Runbook: Order Stuck Pending](../runbooks/order-stuck-pending.md)

### High Latency

**Check**:
1. Database query performance
2. External service calls (Product, Inventory)
3. Kafka consumer lag

**Fix**:
- Add database indexes
- Implement caching
- Optimize queries

---

## Development

### Run Locally

```bash
# Start dependencies
cd docker && docker-compose up -d postgres kafka redis

# Run service
./gradlew :order-service:bootRun
```

### Run Tests

```bash
# Unit tests
./gradlew :order-service:test

# Integration tests
./gradlew :order-service:integrationTest
```

---

## Related Documentation

- [Order Saga Flow](../architecture/order-saga-flow.md)
- [Event Catalog](../architecture/event-catalog.md)
- [API Documentation](../api/README.md)
- [Data Model](../architecture/data-model.md)

---

**Last Updated**: 2026-01-20  
**Maintained By**: Order Team
