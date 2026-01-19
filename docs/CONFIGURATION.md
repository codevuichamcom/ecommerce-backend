# ⚙️ Configuration Reference

Complete reference for all environment variables and configuration properties across the E-commerce Backend services.

---

## Table of Contents

1. [Configuration Overview](#configuration-overview)
2. [Common Configuration](#common-configuration)
3. [Service-Specific Configuration](#service-specific-configuration)
4. [Environment Profiles](#environment-profiles)
5. [Configuration Best Practices](#configuration-best-practices)

---

## Configuration Overview

### Configuration Sources

Configuration is loaded in the following order (later sources override earlier ones):

1. **Default values** in `application.yml`
2. **Environment variables** (e.g., `SPRING_DATASOURCE_URL`)
3. **System properties** (e.g., `-Dspring.profiles.active=prod`)
4. **External config files** (future: Spring Cloud Config)

### Naming Convention

| Format | Example | Usage |
|--------|---------|-------|
| **YAML** | `spring.datasource.url` | application.yml files |
| **Environment Variable** | `SPRING_DATASOURCE_URL` | Docker, Kubernetes |
| **System Property** | `-Dspring.datasource.url` | JVM arguments |

---

## Common Configuration

These configurations are shared across multiple services.

### Database Configuration

**PostgreSQL Connection**

| Variable | Default | Description | Required |
|----------|---------|-------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/{service}_db` | Database JDBC URL | ✅ |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password | ✅ |

**HikariCP Connection Pool**

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.hikari.maximum-pool-size` | `10` | Maximum pool size |
| `spring.datasource.hikari.minimum-idle` | `5` | Minimum idle connections |
| `spring.datasource.hikari.connection-timeout` | `30000` | Connection timeout (ms) |

**Example**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db-server:5432/product_db
    username: app_user
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
```

### JPA/Hibernate Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `spring.jpa.hibernate.ddl-auto` | `validate` | Schema validation mode |
| `spring.jpa.show-sql` | `false` | Log SQL statements |
| `spring.jpa.open-in-view` | `false` | Disable OSIV pattern |
| `spring.jpa.properties.hibernate.format_sql` | `true` | Format SQL output |
| `spring.jpa.properties.hibernate.default_batch_fetch_size` | `20` | Batch fetch size |

**⚠️ Production Override**:
```yaml
spring:
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
```

### Flyway Migration

| Property | Default | Description |
|----------|---------|-------------|
| `spring.flyway.enabled` | `true` | Enable Flyway migrations |
| `spring.flyway.locations` | `classpath:db/migration` | Migration scripts location |
| `spring.flyway.baseline-on-migrate` | `true` | Baseline existing database |

### Redis Configuration

| Variable | Default | Description | Services |
|----------|---------|-------------|----------|
| `SPRING_REDIS_HOST` | `localhost` | Redis host | Product, Order, Gateway |
| `SPRING_REDIS_PORT` | `6379` | Redis port | Product, Order, Gateway |
| `SPRING_REDIS_PASSWORD` | `` | Redis password | Product, Order, Gateway |
| `spring.data.redis.timeout` | `2000ms` | Connection timeout | Order |

**Cache Configuration** (Product Service):
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes
      cache-null-values: false
```

### Kafka Configuration

| Variable | Default | Description | Services |
|----------|---------|-------------|----------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers | Order, Inventory, Payment, Notification |

**Consumer Configuration**:
```yaml
spring:
  kafka:
    consumer:
      group-id: {service}-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

**Producer Configuration**:
```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
```

### Observability Configuration

**Distributed Tracing (Zipkin)**

| Variable | Default | Description |
|----------|---------|-------------|
| `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` | `http://localhost:9411/api/v2/spans` | Zipkin endpoint |
| `management.tracing.sampling.probability` | `1.0` | Sampling rate (0.0-1.0) |

**Actuator Endpoints**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,tracing
  endpoint:
    health:
      show-details: always
```

**⚠️ Production Override**:
```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling in production
  endpoint:
    health:
      show-details: when-authorized
```

### Logging Configuration

**Structured Logging Pattern**:
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %5p [${spring.application.name},%X{traceId:-},%X{spanId:-},%X{userId:-}] %logger{36} - %msg%n"
```

**Log Levels**:
| Property | Default | Production |
|----------|---------|------------|
| `logging.level.root` | `INFO` | `WARN` |
| `logging.level.com.ecommerce` | `DEBUG` | `INFO` |
| `logging.level.org.hibernate.SQL` | `DEBUG` | `WARN` |
| `logging.level.org.springframework.kafka` | `INFO` | `WARN` |

### Virtual Threads (Java 21)

**All services enable Virtual Threads**:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

---

## Service-Specific Configuration

### API Gateway (Port 8080)

**Service Discovery** (Static Routes):
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false  # Using static routes
```

**CORS Configuration**:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
              - OPTIONS
            allowedHeaders: "*"
            maxAge: 3600
```

**⚠️ Production Override**:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: 
              - "https://app.example.com"
              - "https://admin.example.com"
```

**Graceful Shutdown**:
```yaml
server:
  shutdown: graceful
```

---

### Auth Service (Port 8086)

**JWT Configuration**

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | (auto-generated) | JWT signing secret (256-bit) |
| `jwt.access-token-validity-seconds` | `3600` | Access token TTL (1 hour) |
| `jwt.refresh-token-validity-seconds` | `604800` | Refresh token TTL (7 days) |

**Example**:
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-validity-seconds: 1800  # 30 minutes
  refresh-token-validity-seconds: 2592000  # 30 days
```

**🔒 Security Note**: 
- `JWT_SECRET` MUST be set via environment variable in production
- Use a cryptographically secure random string (minimum 256 bits)
- Rotate secrets periodically

**Generate Secret**:
```bash
openssl rand -base64 32
```

---

### Product Service (Port 8081)

**Database**: `product_db`

**SpringDoc OpenAPI**:
```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**Cache**: Redis-based product caching (10 minutes TTL)

---

### Inventory Service (Port 8082)

**Database**: `inventory_db`

**Optimistic Locking**: Enabled via `@Version` on `InventoryJpaEntity`

**Kafka Topics**:
- Consumes: `order-events`
- Produces: `inventory-events`

---

### Order Service (Port 8083)

**Database**: `order_db`

**Service URLs**:
```yaml
services:
  product:
    url: http://localhost:8081
  inventory:
    url: http://localhost:8082
```

**⚠️ Production Override**:
```yaml
services:
  product:
    url: ${PRODUCT_SERVICE_URL:http://product-service:8081}
  inventory:
    url: ${INVENTORY_SERVICE_URL:http://inventory-service:8082}
```

**Jackson Configuration**:
```yaml
spring:
  jackson:
    deserialization:
      fail-on-unknown-properties: false
```

**Kafka Topics**:
- Consumes: `inventory-events`, `payment-events`
- Produces: `order-events`

---

### Payment Service (Port 8084)

**Database**: `payment_db`

**Outbox Polling**:
```yaml
outbox:
  poll:
    interval-ms: 1000  # Poll every 1 second
  cleanup:
    cron: "0 0 3 * * *"  # Cleanup at 3 AM daily
```

**Payment Processing** (Simulation):
```yaml
payment:
  processing:
    delay-ms: 500  # Simulated processing delay
    simulated-failure-rate: 0.1  # 10% failure rate for testing
```

**⚠️ Production Override**:
```yaml
payment:
  processing:
    delay-ms: 0
    simulated-failure-rate: 0.0
```

**Kafka Topics**:
- Consumes: `order-events`
- Produces: `payment-events`

---

### Notification Service (Port 8085)

**Database**: `notification_db`

**Kafka Topics**:
- Consumes: `order-events`, `payment-events`

---

## Environment Profiles

### Development Profile (default)

**File**: `application.yml`

**Characteristics**:
- Local infrastructure (localhost)
- Verbose logging (DEBUG level)
- SQL logging enabled
- 100% tracing sampling
- Show health details

### Test Profile

**File**: `application-test.yml`

**Characteristics**:
- In-memory H2 database (some services)
- Embedded Kafka (TestContainers)
- Minimal logging
- Disabled external integrations

**Activate**:
```bash
./gradlew test  # Automatically uses test profile
```

### Production Profile

**File**: `application-prod.yml` (to be created)

**Recommended Settings**:

```yaml
spring:
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false

management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    root: WARN
    com.ecommerce: INFO
    org.hibernate.SQL: WARN

payment:
  processing:
    simulated-failure-rate: 0.0
```

**Activate**:
```bash
java -jar service.jar --spring.profiles.active=prod
```

---

## Configuration Best Practices

### 1. Externalize Secrets

**❌ Bad**:
```yaml
spring:
  datasource:
    password: postgres123
```

**✅ Good**:
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 2. Use Environment-Specific Overrides

**Development**:
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
```

**Production** (via environment variable):
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/product_db
```

### 3. Document Default Values

Always provide sensible defaults with `${VAR:default}`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### 4. Validate Required Configuration

Use `@ConfigurationProperties` with `@Validated`:

```java
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {
    @NotBlank
    private String secret;
    
    @Min(60)
    private int accessTokenValiditySeconds;
}
```

### 5. Group Related Properties

```yaml
payment:
  processing:
    delay-ms: 500
    simulated-failure-rate: 0.1
  gateway:
    url: https://payment-gateway.example.com
    api-key: ${PAYMENT_API_KEY}
```

---

## Configuration Checklist

### Before Deployment

- [ ] All secrets externalized (no hardcoded passwords)
- [ ] Database URLs point to correct environment
- [ ] Kafka bootstrap servers configured
- [ ] JWT secret set (production)
- [ ] Logging levels appropriate for environment
- [ ] Tracing sampling rate adjusted (production: 0.1)
- [ ] Health endpoint security configured
- [ ] CORS origins restricted (production)
- [ ] Connection pool sizes tuned
- [ ] Simulated failures disabled (production)

---

## Environment Variables Quick Reference

### Required for All Services

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/service_db
export SPRING_DATASOURCE_USERNAME=app_user
export SPRING_DATASOURCE_PASSWORD=secure_password

# Observability
export MANAGEMENT_ZIPKIN_TRACING_ENDPOINT=http://zipkin:9411/api/v2/spans
```

### Required for Kafka-Enabled Services

```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-broker:9092
```

### Required for Redis-Enabled Services

```bash
export SPRING_REDIS_HOST=redis-host
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_PASSWORD=redis_password
```

### Required for Auth Service

```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

### Required for Order Service

```bash
export PRODUCT_SERVICE_URL=http://product-service:8081
export INVENTORY_SERVICE_URL=http://inventory-service:8082
```

---

## Troubleshooting Configuration Issues

### Database Connection Failures

**Symptom**: `Unable to acquire JDBC Connection`

**Check**:
1. Database URL is correct
2. Database is running and accessible
3. Credentials are valid
4. Network connectivity

**Debug**:
```bash
# Test connection
psql -h db-host -U app_user -d service_db

# Check environment variables
env | grep SPRING_DATASOURCE
```

### Kafka Connection Issues

**Symptom**: `Failed to construct kafka consumer`

**Check**:
1. Kafka bootstrap servers are correct
2. Kafka is running
3. Network connectivity
4. Topic exists

**Debug**:
```bash
# List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

# Check environment
env | grep KAFKA
```

### Redis Connection Failures

**Symptom**: `Unable to connect to Redis`

**Check**:
1. Redis host/port are correct
2. Redis is running
3. Password is correct (if required)

**Debug**:
```bash
# Test connection
redis-cli -h redis-host -p 6379 ping

# Check environment
env | grep REDIS
```

---

## Next Steps

- [Development Guide](DEVELOPMENT.md) - Local environment setup
- [Deployment Guide](DEPLOYMENT.md) *(Coming Soon)* - Production deployment
- [Security Guide](SECURITY.md) - Security best practices
- [Troubleshooting](TROUBLESHOOTING.md) *(Coming Soon)* - Common issues

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-19  
**Maintained By**: DevOps Team
