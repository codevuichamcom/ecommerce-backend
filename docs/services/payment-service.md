# Payment Service

**Port**: 8084
**Database**: `payment_db`
**Kafka**: Producer & Consumer

---

## Overview

The Payment Service handles all payment processing, refund logic, and transaction history. It operates asynchronously via Kafka events but also provides REST APIs for status queries.

### Key Responsibilities

- **Process Payments**: Handle payment requests from Order Service.
- **Refunds**: Process refunds for cancelled orders.
- **Transaction History**: maintain immutable record of all transactions.
- **Idempotency**: Ensure payments are processed exactly once.

---

## API Endpoints

### Query Payments

#### Get Payment by ID

```http
GET /api/payments/{id}
Authorization: Bearer {token}
```

#### Get Payment by Order

```http
GET /api/payments/order/{orderId}
Authorization: Bearer {token}
```

#### Get Customer Payments

```http
GET /api/payments?customerId={customerId}
Authorization: Bearer {token}
```

### Operations

#### Refund Payment

```http
POST /api/payments/{id}/refund
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Customer request"
}
```

---

## Kafka Integration

Implemented in `PaymentEventConsumer.java` and `PaymentService.java`.

### Consumed Events (`order-events`)

| Event Type | Action |
|------------|--------|
| `OrderCreated` | Triggers payment processing (creates `Payment` entity) |
| `OrderCancelled` | Triggers refund if payment was completed |

### Produced Events (`payment-events`)

| Event Type | Trigger | Description |
|------------|---------|-------------|
| `PaymentCompleted` | Successful processing | Confirms payment validation |
| `PaymentFailed` | Processing error | Triggers order compensation |
| `PaymentRefunded` | Refund request | Confirms refund processing |

---

## Processing Logic

### Payment Flow

1.  **Ingestion**: `OrderCreated` event received.
2.  **Idempotency Check**: Check if payment exists for `orderId`.
3.  **Processing**:
    - Validate amount/currency.
    - Simulate gateway call (configurable delay).
    - Random failure simulation (configurable rate).
4.  **Completion**:
    - **Success**: Status `COMPLETED`, generate `transactionId`, publish `PaymentCompleted`.
    - **Failure**: Status `FAILED`, publish `PaymentFailed`.

### Simulation Configuration

The service includes logic to simulate real-world payment latency and failures:

```yaml
payment:
  processing:
    delay-ms: 1000        # Latency simulation
    simulated-failure-rate: 0.1 # 10% random failure rate
```

---

## Database Schema

### Payments Table

```sql
CREATE TABLE payments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED, REFUNDED
    transaction_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Outbox & Idempotency

See [Outbox Pattern](../architecture/outbox-pattern.md).

---

## Troubleshooting
 
 ### Transactions Delayed
 - **Cause**: Latency simulation might be enabled. Check `payment.processing.delay-ms`.
 - **Cause**: Kafka consumer lag in `order-events` topic.
 
 ### High Failure Rate
 - **Cause**: Failure simulation active? Check `payment.processing.simulated-failure-rate`.
 - **Check**: External gateway connectivity (mock or real).
 
 ### Duplicate Payments
 - **Check**: Idempotency key logic in `PaymentService`.
 - **Check**: Kafka retries sending duplicate messages without deduplication.
 
 ---
 
 ## Running

```bash
# Run locally
./gradlew :payment-service:bootRun

# Docker
docker run -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/payment_db \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  ecommerce/payment-service
```
