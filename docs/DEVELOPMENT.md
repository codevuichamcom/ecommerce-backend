# 💻 Local Development Guide

Complete guide for setting up and running the E-commerce Backend on your local machine.

---

## Prerequisites

### Required Software

| Software | Version | Download Link |
|----------|---------|---------------|
| **Java SDK** | 21 (LTS) | https://jdk.java.net/21/ |
| **Docker Desktop** | Latest | https://www.docker.com/products/docker-desktop |
| **Gradle** | 8+ | Included via wrapper (`./gradlew`) |
| **Git** | Latest | https://git-scm.com/downloads |

### Recommended Tools

- **IntelliJ IDEA** (Ultimate or Community)
- **Postman** or **Insomnia** for API testing
- **DBeaver** or **pgAdmin** for database management
- **Kafka Tool** or use built-in Kafka UI

### System Requirements

- **RAM**: Minimum 8GB, recommended 16GB
- **Disk Space**: 10GB free space
- **OS**: Linux, macOS, or Windows with WSL2

---

## Quick Start (5 Minutes) ⚡

### 1. Clone Repository

```bash
git clone https://github.com/your-org/ecommerce-backend.git
cd ecommerce-backend
```

### 2. Start Infrastructure

```bash
cd docker
docker-compose up -d
```

### 3. Build All Services

```bash
./gradlew build
```

### 4. Run Services

Open multiple terminals and run:

```bash
# Terminal 1: Product Service
./gradlew :product-service:bootRun

# Terminal 2: Inventory Service
./gradlew :inventory-service:bootRun

# Terminal 3: Order Service
./gradlew :order-service:bootRun

# Terminal 4: Payment Service
./gradlew :payment-service:bootRun

# Terminal 5: Notification Service
./gradlew :notification-service:bootRun

# Terminal 6: Auth Service
./gradlew :auth-service:bootRun

# Terminal 7: API Gateway
./gradlew :api-gateway:bootRun
```

### 5. Verify Setup

```bash
# Check all services are healthy
curl http://localhost:8081/actuator/health  # Product
curl http://localhost:8082/actuator/health  # Inventory
curl http://localhost:8083/actuator/health  # Order
curl http://localhost:8084/actuator/health  # Payment
curl http://localhost:8085/actuator/health  # Notification
curl http://localhost:8086/actuator/health  # Auth
curl http://localhost:8080/actuator/health  # Gateway
```

---

## Detailed Setup

### Infrastructure Components

The `docker-compose.yml` starts these services:

| Component | Port | Purpose | Access |
|-----------|------|---------|--------|
| **PostgreSQL** | 5432 | Database | `postgres:postgres@localhost:5432` |
| **Kafka** | 9092 | Message broker | `localhost:9092` |
| **Zookeeper** | 2181 | Kafka coordination | `localhost:2181` |
| **Redis** | 6379 | Cache & sessions | `localhost:6379` |
| **Zipkin** | 9411 | Distributed tracing | http://localhost:9411 |
| **Prometheus** | 9090 | Metrics collection | http://localhost:9090 |
| **Grafana** | 3000 | Dashboards | http://localhost:3000 |
| **Kafka UI** | 8090 | Kafka management | http://localhost:8090 |

### Database Access

Each service has its own database:

```bash
# Connect to databases
psql -h localhost -U postgres -d product_db
psql -h localhost -U postgres -d inventory_db
psql -h localhost -U postgres -d order_db
psql -h localhost -U postgres -d payment_db
psql -h localhost -U postgres -d auth_db
```

**Default credentials**: `postgres` / `postgres`

**Using DBeaver**:
1. Create new connection → PostgreSQL
2. Host: `localhost`, Port: `5432`
3. Database: `product_db` (or other service DB)
4. Username: `postgres`, Password: `postgres`

### Kafka Access

**View Topics**:
```bash
# Using Kafka UI (recommended)
open http://localhost:8090

# Or using CLI
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Common Topics**:
- `order-events`
- `inventory-events`
- `payment-events`
- `notification-events`

**Consume Messages**:
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### Redis Access

```bash
# Connect to Redis CLI
docker exec -it redis redis-cli

# View all keys
KEYS *

# Get a value
GET idempotency:some-key
```

---

## Development Workflow

### Running Single Service

```bash
# Run with default profile (dev)
./gradlew :product-service:bootRun

# Run with specific profile
./gradlew :product-service:bootRun --args='--spring.profiles.active=local'

# Run with debug enabled
./gradlew :product-service:bootRun --debug-jvm
```

### Running Tests

```bash
# All tests (all services)
./gradlew test

# Single service tests
./gradlew :product-service:test

# Integration tests (requires Docker)
./gradlew integrationTest

# Specific test class
./gradlew :product-service:test --tests ProductServiceTest

# Specific test method
./gradlew :product-service:test --tests ProductServiceTest.shouldCreateProduct
```

### Building Services

```bash
# Build all services
./gradlew build

# Build without tests (faster)
./gradlew build -x test

# Clean build
./gradlew clean build

# Build single service
./gradlew :product-service:build
```

### Viewing Logs

**Application Logs**:
```bash
# Logs are output to console when running with bootRun
# For structured logging, check logs/ directory in each service
```

**Infrastructure Logs**:
```bash
# Docker container logs
docker-compose logs -f postgres
docker-compose logs -f kafka
docker-compose logs -f redis

# All infrastructure logs
docker-compose logs -f
```

---

## IDE Configuration

### IntelliJ IDEA Setup

#### 1. Import Project

1. `File > Open` → Select `ecommerce-backend` directory
2. IntelliJ auto-detects Gradle project
3. Wait for indexing to complete

#### 2. Configure JDK

1. `File > Project Structure > Project`
2. Set **SDK**: Java 21
3. Set **Language Level**: 21 - Record patterns, pattern matching for switch

#### 3. Enable Annotation Processing

1. `Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
2. Check **Enable annotation processing**
3. Apply and restart IDE

#### 4. Configure Code Style

1. `Settings > Editor > Code Style > Java`
2. Import scheme: `config/intellij-code-style.xml` (if available)
3. Or use default Google Java Style

#### 5. Run Configurations

Create run configurations for each service:

1. `Run > Edit Configurations`
2. Click `+` → `Gradle`
3. Name: `Product Service`
4. Gradle project: `ecommerce-backend:product-service`
5. Tasks: `bootRun`
6. Repeat for other services

#### 6. Debugging Configuration

For remote debugging:

1. Run service with debug enabled:
   ```bash
   ./gradlew :product-service:bootRun --debug-jvm
   ```

2. Create Remote JVM Debug configuration:
   - Host: `localhost`
   - Port: `5005` (default)
   - Debugger mode: Attach

3. Set breakpoints and start debugging

---

## Common Development Tasks

### Adding a New Endpoint

1. **Define in Controller** (`infrastructure/web/`):
```java
@PostMapping("/products")
public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
    // Implementation
}
```

2. **Implement Service** (`application/service/`):
```java
public ProductResponse createProduct(CreateProductRequest request) {
    // Business logic
}
```

3. **Add Tests**:
```java
@Test
void shouldCreateProduct() {
    // Test implementation
}
```

4. **Update Swagger Annotations**:
```java
@Operation(summary = "Create a new product")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Product created"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
})
```

### Database Migrations

We use **Flyway** for database migrations:

1. Create migration file in `src/main/resources/db/migration/`:
   ```
   V1__initial_schema.sql
   V2__add_featured_column.sql
   ```

2. Write SQL:
```sql
-- V2__add_featured_column.sql
ALTER TABLE products ADD COLUMN featured BOOLEAN DEFAULT FALSE;
```

3. Restart service → Flyway auto-applies migrations

### Adding Kafka Event

1. **Define Event** (`domain/event/`):
```java
public record ProductCreatedEvent(
    String productId,
    String name,
    BigDecimal price,
    Instant createdAt
) {}
```

2. **Publish Event** (using Outbox):
```java
outboxService.publish(new ProductCreatedEvent(...));
```

3. **Consume Event** (`infrastructure/messaging/`):
```java
@KafkaListener(topics = "product-events")
public void handleProductCreated(ProductCreatedEvent event) {
    // Handle event
}
```

### Accessing Swagger UI

Each service exposes Swagger UI:

- **Product**: http://localhost:8081/swagger-ui.html
- **Inventory**: http://localhost:8082/swagger-ui.html
- **Order**: http://localhost:8083/swagger-ui.html
- **Payment**: http://localhost:8084/swagger-ui.html
- **Notification**: http://localhost:8085/swagger-ui.html
- **Auth**: http://localhost:8086/swagger-ui.html

**OpenAPI JSON**:
- http://localhost:8081/v3/api-docs

---

## Troubleshooting

### Port Already in Use

```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>
```

### Docker Containers Not Starting

```bash
# Stop all containers
docker-compose down

# Remove volumes and restart
docker-compose down -v
docker-compose up -d
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Kafka Issues

```bash
# Check Kafka is running
docker ps | grep kafka

# Restart Kafka and Zookeeper
docker-compose restart zookeeper kafka

# Clear Kafka data (WARNING: deletes all messages)
docker-compose down -v
docker-compose up -d
```

### Build Failures

```bash
# Clean Gradle cache
./gradlew clean

# Clear Gradle daemon
./gradlew --stop

# Rebuild
./gradlew build --refresh-dependencies
```

---

## Performance Tips

### Speed Up Builds

1. **Enable Gradle Daemon** (enabled by default)
2. **Increase Gradle Memory**:
   ```properties
   # gradle.properties
   org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
   ```

3. **Use Build Cache**:
   ```properties
   org.gradle.caching=true
   ```

### Reduce Docker Resource Usage

1. **Limit Container Memory** in `docker-compose.yml`:
   ```yaml
   services:
     postgres:
       mem_limit: 512m
   ```

2. **Stop Unused Services**:
   ```bash
   docker-compose stop grafana prometheus
   ```

---

## Environment Variables

### Common Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `DATABASE_URL` | `localhost:5432` | PostgreSQL connection |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `REDIS_HOST` | `localhost` | Redis host |
| `JWT_SECRET` | (generated) | JWT signing key |

### Override Variables

Create `.env` file in project root:

```bash
# .env
SPRING_PROFILES_ACTIVE=local
DATABASE_URL=localhost:5432
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## Next Steps

- Read [ONBOARDING.md](ONBOARDING.md) for guided 3-day ramp-up
- Explore [Architecture Documentation](architecture/system-overview.md)
- Check [API Documentation](api/README.md)
- Review [Testing Guide](TESTING.md)

---

## Need Help?

- **Common Issues**: See [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- **Slack**: #ecommerce-backend
- **Team Lead**: @tech-lead
