# 📊 Observability Strategy

Complete observability documentation for monitoring, tracing, and logging the E-commerce Backend.

---

## Table of Contents

1. [Observability Overview](#observability-overview)
2. [Distributed Tracing](#distributed-tracing)
3. [Metrics & Monitoring](#metrics--monitoring)
4. [Logging](#logging)
5. [Alerting Rules](#alerting-rules)
6. [Dashboards](#dashboards)
7. [SLI/SLO/SLA](#slislosla)

---

## Observability Overview

### Three Pillars of Observability

```
┌─────────────────────────────────────────┐
│  📍 TRACING                             │
│  - Request flow across services         │
│  - Latency breakdown                    │
│  - Error propagation                    │
├─────────────────────────────────────────┤
│  📈 METRICS                             │
│  - System health (CPU, memory)          │
│  - Business metrics (orders, payments)  │
│  - Performance (latency, throughput)    │
├─────────────────────────────────────────┤
│  📜 LOGGING                             │
│  - Structured logs with context         │
│  - Correlation IDs                      │
│  - Error details                        │
└─────────────────────────────────────────┘
```

### Observability Stack

| Component | Purpose | Access |
|-----------|---------|--------|
| **Zipkin** | Distributed tracing | http://localhost:9411 |
| **Prometheus** | Metrics collection | http://localhost:9090 |
| **Grafana** | Dashboards & visualization | http://localhost:3000 (admin/admin) |
| **Loki** *(Planned)* | Log aggregation | - |

---

## Distributed Tracing

### Zipkin Integration

**Technology**: Micrometer Tracing with Brave

**Configuration**:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (dev)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**Production Override**:
```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling (prod)
```

### Trace Context

**Trace ID**: Unique identifier for entire request flow  
**Span ID**: Unique identifier for single unit of work  
**Parent Span ID**: Links spans in hierarchy

**Example Trace**:
```
Trace ID: 550e8400e29b41d4a716446655440000
├─ Span: POST /api/orders (Gateway) - 245ms
│  ├─ Span: Create Order (Order Service) - 180ms
│  │  ├─ Span: DB Insert (Order Service) - 45ms
│  │  └─ Span: Publish OrderCreated (Order Service) - 15ms
│  ├─ Span: Reserve Stock (Inventory Service) - 120ms
│  │  ├─ Span: DB Update (Inventory Service) - 35ms
│  │  └─ Span: Publish StockReserved (Inventory Service) - 10ms
│  └─ Span: Process Payment (Payment Service) - 350ms
│     ├─ Span: External Gateway Call (Payment Service) - 300ms
│     └─ Span: DB Insert (Payment Service) - 25ms
```

### Kafka Tracing

**Automatic Propagation**: Trace context propagated via Kafka headers

**Configuration**:
```yaml
spring:
  kafka:
    template:
      observation-enabled: true
    listener:
      observation-enabled: true
```

**Trace Headers**:
```
X-B3-TraceId: 550e8400e29b41d4a716446655440000
X-B3-SpanId: 660e8400e29b41d4
X-B3-ParentSpanId: 550e8400e29b41d4
X-B3-Sampled: 1
```

### Using Zipkin UI

**Access**: http://localhost:9411

**Features**:
1. **Find Traces**: Search by service, span name, tags
2. **Trace Timeline**: Visualize request flow
3. **Dependencies**: Service dependency graph
4. **Latency Analysis**: Identify slow spans

**Example Queries**:
```
serviceName=order-service
minDuration=1000ms
tag=http.status_code=500
```

---

## Metrics & Monitoring

### Prometheus Metrics

**Endpoint**: `/actuator/prometheus`

**Scrape Configuration**:
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'product-service:8081'
        - 'inventory-service:8082'
        - 'order-service:8083'
        - 'payment-service:8084'
        - 'notification-service:8085'
        - 'auth-service:8086'
        - 'api-gateway:8080'
    scrape_interval: 15s
```

### Standard Metrics

#### JVM Metrics

| Metric | Description |
|--------|-------------|
| `jvm_memory_used_bytes` | Memory usage by pool |
| `jvm_memory_max_bytes` | Maximum memory |
| `jvm_gc_pause_seconds` | GC pause duration |
| `jvm_threads_live` | Live thread count |
| `jvm_classes_loaded` | Loaded class count |

**Example Query**:
```promql
# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

#### HTTP Metrics

| Metric | Description |
|--------|-------------|
| `http_server_requests_seconds_count` | Request count |
| `http_server_requests_seconds_sum` | Total request duration |
| `http_server_requests_seconds_max` | Maximum request duration |

**Example Queries**:
```promql
# Request rate (requests per second)
rate(http_server_requests_seconds_count[5m])

# Average latency
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# P95 latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

#### Database Metrics

| Metric | Description |
|--------|-------------|
| `hikaricp_connections_active` | Active connections |
| `hikaricp_connections_idle` | Idle connections |
| `hikaricp_connections_pending` | Pending connections |
| `hikaricp_connections_timeout_total` | Connection timeouts |

**Example Query**:
```promql
# Connection pool utilization
(hikaricp_connections_active / hikaricp_connections_max) * 100
```

#### Kafka Metrics

| Metric | Description |
|--------|-------------|
| `kafka_consumer_lag` | Consumer lag |
| `kafka_consumer_records_consumed_total` | Records consumed |
| `kafka_producer_record_send_total` | Records produced |

**Example Query**:
```promql
# Consumer lag
kafka_consumer_lag{topic="order-events"}
```

### Custom Business Metrics

**Implementation**:
```java
@Component
public class OrderMetrics {
    
    private final Counter ordersCreated;
    private final Counter ordersCompleted;
    private final Counter ordersCancelled;
    
    public OrderMetrics(MeterRegistry registry) {
        this.ordersCreated = Counter.builder("orders_created_total")
            .description("Total orders created")
            .tag("service", "order-service")
            .register(registry);
            
        this.ordersCompleted = Counter.builder("orders_completed_total")
            .description("Total orders completed")
            .tag("service", "order-service")
            .register(registry);
            
        this.ordersCancelled = Counter.builder("orders_cancelled_total")
            .description("Total orders cancelled")
            .tag("service", "order-service")
            .register(registry);
    }
    
    public void incrementOrdersCreated() {
        ordersCreated.increment();
    }
}
```

**Business Metrics Catalog**:

| Metric | Type | Description | Service |
|--------|------|-------------|---------|
| `orders_created_total` | Counter | Total orders initiated | Order |
| `orders_completed_total` | Counter | Successfully completed orders | Order |
| `orders_cancelled_total` | Counter | Cancelled orders | Order |
| `payment_success_total` | Counter | Successful payments | Payment |
| `payment_failed_total` | Counter | Failed payments | Payment |
| `inventory_reservation_success_total` | Counter | Successful stock reservations | Inventory |
| `inventory_reservation_failed_total` | Counter | Failed stock reservations | Inventory |

**Example Queries**:
```promql
# Order completion rate
rate(orders_completed_total[5m]) / rate(orders_created_total[5m])

# Payment success rate
rate(payment_success_total[5m]) / (rate(payment_success_total[5m]) + rate(payment_failed_total[5m]))
```

---

## Logging

### Structured Logging

**Log Pattern**:
```
%d{yyyy-MM-dd HH:mm:ss} [%thread] %5p [${spring.application.name},%X{traceId:-},%X{spanId:-},%X{userId:-}] %logger{36} - %msg%n
```

**Example Log**:
```
2026-01-19 10:15:30 [http-nio-8083-exec-1] INFO [order-service,550e8400e29b41d4a716446655440000,660e8400e29b41d4,customer-123] c.e.order.OrderService - Order created: 01HQZX3Y4Z5A6B7C8D9E0F1G2H
```

**Log Fields**:
- **Timestamp**: `2026-01-19 10:15:30`
- **Thread**: `http-nio-8083-exec-1`
- **Level**: `INFO`
- **Service**: `order-service`
- **Trace ID**: `550e8400e29b41d4a716446655440000`
- **Span ID**: `660e8400e29b41d4`
- **User ID**: `customer-123`
- **Logger**: `c.e.order.OrderService`
- **Message**: `Order created: 01HQZX3Y4Z5A6B7C8D9E0F1G2H`

### MDC (Mapped Diagnostic Context)

**Automatic Context**:
- `traceId`: From Micrometer Tracing
- `spanId`: From Micrometer Tracing

**Custom Context**:
```java
@Component
public class UserContextFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String userId = extractUserId(request);
        MDC.put("userId", userId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
        }
    }
}
```

### Log Levels

| Environment | Root | Application | SQL | Kafka |
|-------------|------|-------------|-----|-------|
| **Development** | INFO | DEBUG | DEBUG | INFO |
| **Staging** | INFO | INFO | WARN | INFO |
| **Production** | WARN | INFO | WARN | WARN |

**Configuration**:
```yaml
logging:
  level:
    root: INFO
    com.ecommerce: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.kafka: INFO
```

### Log Aggregation (Planned)

**Loki + Grafana**:
```yaml
# promtail.yml
clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: spring-boot
    static_configs:
      - targets:
          - localhost
        labels:
          job: spring-boot
          __path__: /var/log/spring-boot/*.log
```

---

## Alerting Rules

### Prometheus Alert Rules

**Configuration**:
```yaml
# alerts.yml
groups:
  - name: service_health
    interval: 30s
    rules:
      # Service Down
      - alert: ServiceDown
        expr: up{job="spring-boot"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.instance }} is down"
          description: "{{ $labels.instance }} has been down for more than 1 minute"
      
      # High Error Rate
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate on {{ $labels.instance }}"
          description: "Error rate is {{ $value | humanizePercentage }}"
      
      # High Latency
      - alert: HighLatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High latency on {{ $labels.instance }}"
          description: "P95 latency is {{ $value }}s"
      
      # Database Connection Pool Exhausted
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool near exhaustion"
          description: "Connection pool is {{ $value | humanizePercentage }} full"
      
      # High Memory Usage
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage on {{ $labels.instance }}"
          description: "Memory usage is {{ $value | humanizePercentage }}"
      
      # Kafka Consumer Lag
      - alert: KafkaConsumerLag
        expr: kafka_consumer_lag > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High Kafka consumer lag"
          description: "Consumer lag is {{ $value }} messages"
      
      # Payment Failure Rate
      - alert: HighPaymentFailureRate
        expr: rate(payment_failed_total[5m]) / (rate(payment_success_total[5m]) + rate(payment_failed_total[5m])) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High payment failure rate"
          description: "Payment failure rate is {{ $value | humanizePercentage }}"
```

### Alert Severity Levels

| Severity | Response Time | Notification | Example |
|----------|---------------|--------------|---------|
| **Critical** | Immediate | PagerDuty | Service down, high error rate |
| **Warning** | 15 minutes | Slack | High latency, high memory |
| **Info** | Next business day | Email | Deployment completed |

### Alertmanager Configuration

```yaml
# alertmanager.yml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
    - match:
        severity: warning
      receiver: 'slack'

receivers:
  - name: 'default'
    email_configs:
      - to: 'team@example.com'
  
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: '<pagerduty-key>'
  
  - name: 'slack'
    slack_configs:
      - api_url: '<slack-webhook-url>'
        channel: '#ecommerce-alerts'
```

---

## Dashboards

### Grafana Dashboards

**Access**: http://localhost:3000 (admin/admin)

#### 1. Service Health Overview

**Panels**:
- Service uptime (%)
- Request rate (req/s)
- Error rate (%)
- P50/P95/P99 latency
- Active instances

**Query Examples**:
```promql
# Uptime
avg(up{job="spring-boot"}) by (instance)

# Request rate
sum(rate(http_server_requests_seconds_count[5m])) by (instance)

# Error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

#### 2. JVM Metrics

**Panels**:
- Heap memory usage
- GC pause time
- Thread count
- Class loading

#### 3. Database Performance

**Panels**:
- Connection pool usage
- Query execution time
- Transaction rate
- Deadlocks

#### 4. Kafka Metrics

**Panels**:
- Consumer lag by topic
- Messages consumed/produced
- Consumer group status

#### 5. Business Metrics

**Panels**:
- Orders created (per hour)
- Order completion rate
- Payment success rate
- Revenue (per hour)

---

## SLI/SLO/SLA

### Service Level Indicators (SLI)

**Availability**:
```promql
# Percentage of successful requests
sum(rate(http_server_requests_seconds_count{status!~"5.."}[30d])) / sum(rate(http_server_requests_seconds_count[30d]))
```

**Latency**:
```promql
# P95 latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

**Error Rate**:
```promql
# Percentage of failed requests
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

### Service Level Objectives (SLO)

| Metric | SLO | Measurement Window |
|--------|-----|-------------------|
| **Availability** | 99.9% | 30 days |
| **Latency (P95)** | < 500ms | 5 minutes |
| **Error Rate** | < 1% | 5 minutes |
| **Order Completion Rate** | > 95% | 1 hour |

### Service Level Agreements (SLA)

**Customer-Facing SLAs**:

| Service | Availability | Latency | Support |
|---------|--------------|---------|---------|
| **API Gateway** | 99.9% | P95 < 200ms | 24/7 |
| **Order Processing** | 99.5% | P95 < 1s | Business hours |
| **Payment Processing** | 99.9% | P95 < 2s | 24/7 |

**Error Budget**:
```
Monthly Error Budget = (1 - SLO) × Total Requests
Example: (1 - 0.999) × 10M = 10,000 failed requests allowed per month
```

---

## Monitoring Best Practices

### ✅ Do

- Set up alerts for critical metrics
- Use structured logging with correlation IDs
- Monitor business metrics, not just technical metrics
- Set realistic SLOs based on user expectations
- Review dashboards regularly
- Test alerting rules

### ❌ Don't

- Alert on everything (alert fatigue)
- Log sensitive data (passwords, tokens)
- Ignore warning alerts
- Set SLOs too high (99.999% is expensive)
- Forget to update dashboards

---

## Next Steps

- [Troubleshooting Guide](TROUBLESHOOTING.md) - Use metrics to debug issues
- [Deployment Guide](DEPLOYMENT.md) - Monitor deployments
- [Runbooks](runbooks/) *(Coming Soon)* - Alert response procedures
- [Security Guide](SECURITY.md) - Security monitoring

---

**Document Version**: 2.0  
**Last Updated**: 2026-01-19  
**Maintained By**: SRE Team
