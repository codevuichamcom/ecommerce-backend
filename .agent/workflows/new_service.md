---
description: Creating a new microservice
---

# New Service Workflow

## Overview
This workflow guides the creation of a new microservice in the ecommerce-backend platform.

---

## Steps

### 1. Define Service Responsibility
Answer these questions:
- **What domain does this service own?**
- **What are the service boundaries?**
- **Why can't existing services handle this?**
- **What data will this service manage?**
- **What business capabilities does it provide?**

Document the answers clearly.

### 2. Define API Contract
Design the REST API:
- **Endpoints**: List all endpoints with HTTP methods
- **Request DTOs**: Define input structures
- **Response DTOs**: Define output structures
- **Error Responses**: Define error formats (RFC 7807)
- **Authentication**: How will it authenticate requests?

Example:
```
POST /api/v1/shipments
GET /api/v1/shipments/{id}
PUT /api/v1/shipments/{id}/status
```

### 3. Design Domain Model
Define the core domain:
- **Entities**: What are the main domain objects?
- **Value Objects**: What are the immutable values?
- **Domain Events**: What events will be published?
- **Repository Interfaces**: What data access is needed?
- **Domain Services**: What domain logic doesn't fit in entities?

### 4. Human Review Architecture ⏸️
**STOP HERE - Wait for approval**
- Present the service design
- Document in `docs/architecture/`
- Get feedback on:
  - Service boundaries
  - API design
  - Domain model
  - Integration points
- Update design based on review

### 5. Create Service Skeleton

#### 5.1 Add to Gradle
Edit `settings.gradle.kts`:
```kotlin
include("new-service")
```

#### 5.2 Create `build.gradle.kts`
Copy from existing service and modify:
```kotlin
plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":common-lib"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Add other dependencies
}
```

#### 5.3 Create Directory Structure
```
new-service/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/newservice/
│   │   │   ├── NewServiceApplication.java
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   └── service/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── event/
│   │   │   └── infrastructure/
│   │   │       ├── config/
│   │   │       ├── persistence/
│   │   │       └── messaging/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/ecommerce/newservice/
└── build.gradle.kts
```

#### 5.4 Create Application Class
```java
package com.ecommerce.newservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewServiceApplication.class, args);
    }
}
```

#### 5.5 Create `application.yml`
```yaml
spring:
  application:
    name: new-service
  datasource:
    url: jdbc:postgresql://localhost:5432/newservice_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8087  # Choose next available port
```

#### 5.6 Add Flyway Migration
Create `V1__init_schema.sql`:
```sql
-- Initial schema for new-service
CREATE TABLE IF NOT EXISTS example_entity (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);
```

### 6. Implement Core Features
Follow hexagonal architecture:

#### 6.1 Domain Layer
- Create entities with `@Version` for optimistic locking
- Create value objects as `record`
- Define repository interfaces (ports)
- Create domain events

#### 6.2 Application Layer
- Create DTOs as `record`
- Implement service classes
- Handle use cases

#### 6.3 Infrastructure Layer
- Implement JPA repositories
- Create JPA entities (separate from domain)
- Implement Kafka producers/consumers if needed
- Add REST controllers

### 7. Add to Docker Compose
Edit `docker/docker-compose.yml`:
```yaml
new-service:
  build:
    context: ..
    dockerfile: Dockerfile
    args:
      SERVICE_NAME: new-service
  ports:
    - "8087:8087"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/newservice_db
  depends_on:
    - postgres
    - kafka
```

### 8. Add to API Gateway
Edit `api-gateway/src/main/resources/application.yml`:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-service
          uri: http://localhost:8087
          predicates:
            - Path=/api/v1/new-service/**
```

### 9. Write Tests
- Unit tests for domain logic
- Unit tests for services
- Integration tests with Testcontainers
- API contract tests

### 10. Documentation
Create `docs/NEW_SERVICE.md`:
- Service overview
- API documentation
- Domain model
- Integration points
- How to run locally

---

## Example Checklist

- [ ] Service responsibility defined
- [ ] API contract designed
- [ ] Domain model designed
- [ ] Architecture documented
- [ ] Human approval received
- [ ] Gradle configuration added
- [ ] Directory structure created
- [ ] Application class created
- [ ] application.yml configured
- [ ] Flyway migrations created
- [ ] Domain layer implemented
- [ ] Application layer implemented
- [ ] Infrastructure layer implemented
- [ ] Docker Compose updated
- [ ] API Gateway routing added
- [ ] Tests written (coverage ≥70%)
- [ ] Documentation created
- [ ] Service runs locally
- [ ] PR created
