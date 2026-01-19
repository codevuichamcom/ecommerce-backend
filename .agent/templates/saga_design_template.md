# Saga Pattern Design Template

## Saga Overview
- **Saga Name**: {saga_name}
- **Type**: Orchestration (Recommended) / Choreography
- **Trigger**: {What initiates this saga}
- **Business Goal**: {What business transaction this saga completes}

## Participants (Services)
1. **{Service 1}**: {Responsibility}
2. **{Service 2}**: {Responsibility}
3. **{Service 3}**: {Responsibility}

## Saga Steps (Happy Path)
1. **{Service 1}**: {Action} → Publishes `{event_name}`
2. **{Service 2}**: {Action} → Publishes `{event_name}`
3. **{Service 3}**: {Action} → Publishes `{saga_completed_event}`

## Compensation Logic (Rollback)
If step N fails, compensate in reverse order:
1. **Compensate Step N-1**: {Service X} executes `{compensation_action}`
2. **Compensate Step N-2**: {Service Y} executes `{compensation_action}`
3. **Publish**: `{saga_failed_event}`

## State Persistence
- **Storage**: Database table `saga_state` (service: {orchestrator_service})
- **Fields**: `saga_id`, `saga_type`, `current_step`, `status`, `payload`, `created_at`, `updated_at`

## Timeout Configuration
- **Step timeout**: {duration} (e.g., 30 seconds)
- **Compensation timeout**: {duration} (e.g., 60 seconds)
- **Total saga timeout**: {duration} (e.g., 5 minutes)

## Error Handling
- **Transient errors**: Retry 3 times with exponential backoff
- **Permanent errors**: Trigger compensation immediately
- **Timeout**: Trigger compensation after timeout expires

## Monitoring
- **Metrics**: Track saga completion rate, compensation rate, average duration
- **Alerts**: Alert if compensation rate > 5% or saga duration > {threshold}

## Example: Order Saga
```
1. Order Service: Reserve Order → order.reserved
2. Inventory Service: Reserve Stock → inventory.reserved
3. Payment Service: Charge Payment → payment.completed
4. Order Service: Confirm Order → order.confirmed

Compensation (if Payment fails):
1. Inventory Service: Release Stock
2. Order Service: Cancel Order
```
