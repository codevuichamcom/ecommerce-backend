# 📊 Observability Strategy

Our system implements the three pillars of observability: **Tracing**, **Metrics**, and **Logging**.

## 🕵️ Distributed Tracing (Zipkin)

We use **Micrometer Tracing** with **Brave** to track requests across service boundaries, including asynchronous Kafka communication.

*   **Trace ID**: Unique ID for a single request flow.
*   **Span ID**: Unique ID for a single unit of work within a service.
*   **Zipkin UI**: Accessible at `http://localhost:9411` in development.

### Kafka Tracing
Trace context is automatically propagated through Kafka headers, allowing us to see the full flow from Order creation to Payment and Inventory updates.

## 📈 Metrics (Prometheus & Grafana)

All services expose a `/actuator/prometheus` endpoint.

*   **Prometheus**: Scrapes metrics from all instances every 15 seconds.
*   **Grafana**: Visualizes metrics in dashboards (accessible at `http://localhost:3000`).

### Custom Business Metrics

| Metric Name | Type | Description |
| :--- | :--- | :--- |
| `order_created_total` | Counter | Total number of orders initiated. |
| `order_completed_total` | Counter | Total number of successfully completed orders. |
| `payment_success_total` | Counter | Successful payment transactions. |
| `payment_failed_total` | Counter | Failed payment attempts. |
| `inventory_reservation_total` | Counter | Success/Failure of stock reservations. |

## 📜 Unified Logging (MDC)

Logs are standardized across all services to include correlation IDs.

**Pattern**: `[%d{yyyy-MM-dd HH:mm:ss}] [%thread] %5p [${spring.application.name},%X{traceId:-},%X{spanId:-},%X{userId:-}] %logger{36} - %msg%n`

*   **Trace ID Integration**: Search logs in any service using a single Trace ID found in Zipkin.
*   **User ID Integration**: Track all actions performed by a specific user across the entire system.

## 🚀 Monitoring Infrastructure

Total observability stack in `docker-compose.yml`:

1.  **Zipkin**: Distributed tracing server.
2.  **Prometheus**: Time-series database for metrics.
3.  **Grafana**: Dashboarding tool.

---

To view metrics:
```bash
# Prometheus UI
open http://localhost:9090

# Grafana Dashboard (admin/admin)
open http://localhost:3000
```
