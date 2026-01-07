# Order Creation Saga Flow

The platform uses a **Saga Pattern** (Choreography/Orchestration mix) to manage distributed transactions across microservices.

## 🚀 Happy Path Flow

The diagram below shows the sequence of events during a successful order creation.

```mermaid
sequenceDiagram
    participant U as User
    participant OS as Order Service
    participant K as Kafka
    participant IS as Inventory Service
    participant PayS as Payment Service
    participant NS as Notification Service

    U->>OS: POST /api/v1/orders
    Note over OS: 1. Save Order (PENDING)<br/>2. Save Saga (STARTED)
    OS-->>K: Publish OrderCreated (Outbox)
    OS->>U: 202 Accepted (OrderId)
    
    K->>IS: OrderCreated
    Note over IS: 3. Reserve Stock
    IS-->>K: Publish AllItemsReserved
    
    K->>OS: AllItemsReserved
    Note over OS: 4. Update Saga (INVENTORY_RESERVED)
    OS-->>K: Publish PaymentRequested (Outbox)
    
    K->>PayS: PaymentRequested
    Note over PayS: 5. Process Payment
    PayS-->>K: Publish PaymentCompleted
    
    K->>OS: PaymentCompleted
    Note over OS: 6. Update Saga (COMPLETED)<br/>7. Confirm Order
    OS-->>K: Publish OrderConfirmed (Outbox)
    
    K->>NS: OrderConfirmed
    Note over NS: 8. Send Email Notification
```

## 🛠️ State Management

The `OrderSaga` in the Order Service tracks the progress of each order:

*   **STARTED**: Initial state after receiving the order request.
*   **INVENTORY_RESERVED**: Stock has been successfully allocated.
*   **PAYMENT_COMPLETED**: Funds have been successfully processed.
*   **COMPLETED**: All steps finished successfully.
*   **FAILED**: Something went wrong, triggering compensation or cancellation.

## 🔄 Compensation Flows

If a step fails, the system triggers compensating transactions:

1.  **Inventory Failed**: If stock cannot be reserved, the Order Service cancels the order.
2.  **Payment Failed**: If payment fails, the Order Service:
    *   Publishes a compensation event to Release Inventory.
    *   Cancels the Order.
