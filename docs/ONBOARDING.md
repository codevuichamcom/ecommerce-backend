# 🚀 Developer Onboarding Guide

Welcome to the E-commerce Backend team! This guide will help you become productive within **3 days**.

---

## 📋 Prerequisites Checklist

Before you start, ensure you have:

- [ ] **Java 21 SDK** installed ([Download](https://jdk.java.net/21/))
- [ ] **Docker Desktop** running ([Download](https://www.docker.com/products/docker-desktop))
- [ ] **IntelliJ IDEA** (recommended) or your preferred IDE
- [ ] **Git** configured with your credentials
- [ ] Access to team **Slack channels** (#ecommerce-backend, #ecommerce-alerts)
- [ ] Repository access granted

---

## Day 1: Environment Setup ⚙️

### 1.1 Clone Repository

```bash
git clone https://github.com/your-org/ecommerce-backend.git
cd ecommerce-backend
```

### 1.2 Start Infrastructure

Start PostgreSQL, Kafka, Redis, and observability stack:

```bash
cd docker
docker-compose up -d
```

Verify all containers are running:
```bash
docker-compose ps
```

You should see:
- PostgreSQL (port 5432)
- Kafka + Zookeeper (port 9092)
- Redis (port 6379)
- Zipkin (port 9411)
- Prometheus (port 9090)
- Grafana (port 3000)

### 1.3 Build Project

```bash
./gradlew build
```

This will:
- Download dependencies
- Compile all services
- Run unit tests
- Generate build artifacts

**Expected time**: 3-5 minutes on first run

### 1.4 IDE Setup (IntelliJ IDEA)

1. **Open Project**: `File > Open` → Select `ecommerce-backend` directory
2. **Import as Gradle Project**: IntelliJ should auto-detect
3. **Set JDK**: `File > Project Structure > Project SDK` → Select Java 21
4. **Enable Annotation Processing**: `Settings > Build > Compiler > Annotation Processors` → Check "Enable annotation processing"

**Recommended Plugins**:
- Lombok
- Docker
- Database Navigator
- Rainbow Brackets

### 1.5 Run Your First Service

Start the Product Service:

```bash
./gradlew :product-service:bootRun
```

Verify it's running:
```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 1.6 Access Swagger UI

Open in browser:
- **Product Service**: http://localhost:8081/swagger-ui.html
- **Inventory Service**: http://localhost:8082/swagger-ui.html
- **Order Service**: http://localhost:8083/swagger-ui.html

Try the `GET /api/products` endpoint!

---

## Day 2: Architecture Understanding 🏗️

### 2.1 System Overview

Read the **High-Level Design**:
- [System Architecture Overview](architecture/system-overview.md)

**Key Concepts**:
- **Microservices Architecture**: 7 independent services
- **Event-Driven**: Kafka for async communication
- **Hexagonal Architecture**: Domain-driven design
- **Database per Service**: Each service owns its data

### 2.2 Service Responsibilities

| Service | Port | What It Does |
|---------|------|--------------|
| **API Gateway** | 8080 | Routes requests, auth validation |
| **Auth Service** | 8086 | User authentication, JWT tokens |
| **Product Service** | 8081 | Product catalog management |
| **Inventory Service** | 8082 | Stock tracking, reservations |
| **Order Service** | 8083 | Order orchestration (Saga coordinator) |
| **Payment Service** | 8084 | Payment processing |
| **Notification Service** | 8085 | Email/SMS notifications |

### 2.3 Key Patterns

Read these pattern documents:

1. **Saga Pattern**: [Order Saga Flow](architecture/order-saga-flow.md)
   - How distributed transactions work
   - Compensation flows when things fail

2. **Outbox Pattern**: [Transactional Outbox](architecture/outbox-pattern.md)
   - Guaranteed event delivery
   - Why we don't lose messages

### 2.4 Data Flow Walkthrough

**Example: Creating an Order**

1. Customer calls `POST /api/orders` → **API Gateway**
2. Gateway forwards to **Order Service**
3. Order Service:
   - Creates order (status: PENDING)
   - Saves `OrderCreated` event to outbox
   - Returns order ID to customer
4. Outbox publisher sends `OrderCreated` to Kafka
5. **Inventory Service** consumes event → Reserves stock → Publishes `StockReserved`
6. **Order Service** consumes `StockReserved` → Publishes `RequestPayment`
7. **Payment Service** processes payment → Publishes `PaymentCompleted`
8. **Order Service** confirms order → Publishes `OrderConfirmed`
9. **Notification Service** sends confirmation email

**If payment fails**: Order Service triggers compensation (releases stock, cancels order)

### 2.5 Explore the Codebase

Each service follows **Hexagonal Architecture**:

```
service-name/
├── domain/              # Pure business logic (no frameworks)
│   ├── model/          # Entities, Value Objects
│   ├── port/           # Interfaces (contracts)
│   └── event/          # Domain events
├── application/         # Use cases, orchestration
│   └── service/        # Application services
└── infrastructure/      # External adapters
    ├── web/            # REST controllers
    ├── persistence/    # JPA repositories
    ├── messaging/      # Kafka consumers
    └── security/       # Auth filters
```

**Exercise**: Navigate to `product-service` and find:
- Domain entity: `Product.java`
- REST controller: `ProductController.java`
- Repository: `ProductRepository.java`

---

## Day 3: First Contribution 🎯

### 3.1 Development Workflow

We follow **Git Flow**:

```bash
# Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name

# Make changes, commit frequently
git add .
git commit -m "feat: add product search endpoint"

# Push and create PR
git push origin feature/your-feature-name
```

### 3.2 Guided Exercise: Add a Simple Feature

**Task**: Add a "featured" flag to products

**Steps**:

1. **Update Domain Model** (`Product.java`):
```java
private boolean featured = false;
```

2. **Update Database Schema**:
```sql
ALTER TABLE products ADD COLUMN featured BOOLEAN DEFAULT FALSE;
```

3. **Update REST API** (`ProductController.java`):
```java
@GetMapping("/featured")
public List<ProductResponse> getFeaturedProducts() {
    return productService.getFeaturedProducts();
}
```

4. **Write Tests** (`ProductServiceTest.java`):
```java
@Test
void shouldReturnOnlyFeaturedProducts() {
    // Given
    // When
    // Then
}
```

5. **Run Tests**:
```bash
./gradlew :product-service:test
```

6. **Create Pull Request** using our [PR template](.github/pull_request_template.md)

### 3.3 Code Review Process

1. Push your branch
2. Create PR on GitHub
3. Request review from team lead
4. Address feedback
5. Once approved, squash and merge to `develop`

### 3.4 Running Tests

```bash
# Unit tests (fast)
./gradlew test

# Integration tests (requires Docker)
./gradlew integrationTest

# Specific service
./gradlew :product-service:test
```

---

## 📚 Resources

### Documentation

- [Development Guide](DEVELOPMENT.md) - Detailed local setup
- [Architecture Docs](architecture/) - Design decisions
- [API Documentation](api/) - OpenAPI specs
- [Troubleshooting](TROUBLESHOOTING.md) - Common issues

### Tools & Dashboards

- **Swagger UI**: http://localhost:808X/swagger-ui.html (X = service port)
- **Kafka UI**: http://localhost:8090
- **Zipkin Tracing**: http://localhost:9411
- **Grafana Dashboards**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

### Slack Channels

- **#ecommerce-backend**: General discussions
- **#ecommerce-alerts**: Production alerts
- **#ecommerce-deployments**: Deployment notifications

### Key Contacts

- **Tech Lead**: @tech-lead
- **DevOps**: @devops-team
- **Architect**: @solution-architect

### Learning Materials

- [Spring Boot 3 Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kafka Fundamentals](https://kafka.apache.org/documentation/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)

---

## ✅ Onboarding Checklist

By the end of Day 3, you should have:

- [ ] Successfully built and run all services
- [ ] Understood the system architecture
- [ ] Explored the codebase structure
- [ ] Made your first code contribution
- [ ] Created your first Pull Request
- [ ] Run tests successfully
- [ ] Accessed all monitoring tools

---

## 🆘 Need Help?

- **Stuck on setup?** Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- **Architecture questions?** Read [architecture/system-overview.md](architecture/system-overview.md)
- **Code questions?** Ask in #ecommerce-backend Slack channel
- **Urgent issues?** Contact your team lead

---

**Welcome aboard! 🎉**
