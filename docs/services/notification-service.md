# Notification Service

**Port**: 8085
**Database**: `notification_db`
**Kafka**: Consumer

---

## Overview

The Notification Service manages all communication with users. It listens to system events and sends notifications (Email, SMS, Push) based on business rules.

### Key Features

- **Multi-channel Support**: Designed for Email, SMS, etc (Currently Email only).
- **Template Management**: Standardized messages for system events.
- **Event Driven**: Reacts asynchronously to orders and payments.
- **History**: Keeps track of all sent notifications.

---

## API Endpoints

### Query Notifications

#### Get Notification by ID

```http
GET /api/notifications/{id}
Authorization: Bearer {token}
```

#### Get My Notifications

```http
GET /api/notifications/recipient/{recipientId}
Authorization: Bearer {token}
```

---

## Kafka Integration

Implemented in `NotificationEventConsumer.java`.

### Consumed Events

The service acts as a consumer for multiple topics:

| Topic | Event Type | Action |
|-------|------------|--------|
| `order-events` | `OrderConfirmed` | Send order confirmation email |
| `order-events` | `OrderCancelled` | Send cancellation email |
| `payment-events` | `PaymentCompleted` | Send payment receipt email |
| `payment-events` | `PaymentFailed` | Send payment failure alert |

---

## Templates & Logic

### Email Logic

Currently, specific templates are hardcoded in `NotificationService.java`.

1.  **Order Confirmed**:
    - Subject: `Order Confirmed - #{orderId}`
    - Body: `Your order #{orderId} has been confirmed. Total: {amount}`

2.  **Payment Received**:
    - Subject: `Payment Received`
    - Body: `We have received your payment of {amount} for order #{orderId}. Transaction ID: {txnId}`

3.  **Order Cancelled**:
    - Subject: `Order Cancelled`
    - Body: `Your order #{orderId} has been cancelled. Reason: {reason}`

4.  **Payment Failed**:
    - Subject: `Payment Failed`
    - Body: `We were unable to process your payment for order #{orderId}. Reason: {reason}...`

### Recipient Resolution

*Current Implementation*: The recipient email is inferred from the `customerId` pattern: `{customerId}@example.com`.
*Future Info*: Will integrate with User Service to fetch real user contact details.

### Delivery Providers

- **Dev/Test**: `MockEmailSender` (Outputs to logs).
- **Production**: To be implemented (SMTP/SendGrid/AWS SES).

---

## Database Schema

### Notifications Table

```sql
CREATE TABLE notifications (
    id VARCHAR(100) PRIMARY KEY,
    recipient_id VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, SENT, FAILED
    provider_message_id VARCHAR(100), -- ID from external provider
    metadata TEXT, -- JSON string
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT
);
```

---

## Troubleshooting
 
 ### Emails Not Sent
 - **Log Check**: Look for `MailSendException`.
 - **Config**: Verify SMTP properties in `application.yml`.
 - **Dev Mode**: If using `MockEmailSender`, emails only appear in logs, not real inboxes.
 
 ### Kafka Consumption Stopped
 - **Check**: Is the consumer group `notification-group` active?
 - **Check**: Are topics `order-events` and `payment-events` populated?
 
 ---
 
 ## Running

```bash
# Run locally
./gradlew :notification-service:bootRun

# Docker
docker run -p 8085:8085 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/notification_db \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  ecommerce/notification-service
```
