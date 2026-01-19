---
description: Creating a new microservice
---

# New Service Workflow

## Steps

### 1. Design
- **Define**: Service responsibility, API contract, and Domain model.
- **Template**: [.agent/templates/service_design_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/service_design_template.md)

### 2. Architecture Review ⏸️
- Document design in `docs/architecture/` and get approval from Tech Lead.

### 3. Service Skeleton
- **Gradle**: Add to `settings.gradle.kts` and create `build.gradle.kts` (copy from existing).
- **Structure**: Create Hexagonal structure (Application, Domain, Infrastructure).
- **Application**: Create `SpringBootApplication` class and `application.yml`.
- **Database**: Add initial Flyway migration (`V1__init_schema.sql`).

### 4. Implementation
- **Domain**: Entities (`@Version`), Value Objects (`record`), Repositories (ports).
- **Application**: DTOs (`record`), Service implementations.
- **Infrastructure**: Persistence (JPA), Messaging (Kafka), Controllers (REST).

### 5. Integration
- **Docker**: Add service to `docker-compose.yml`.
- **Gateway**: Add routing rules to `api-gateway/src/main/resources/application.yml`.

### 6. Verification
- **Test**: Unit tests, Integration tests (Testcontainers), Contract tests.
- **Quality**: Ensure coverage (Service ≥70%, Domain ≥80%).

## Checklist
- [ ] Service design approved by Tech Lead
- [ ] Hexagonal structure implemented
- [ ] Database migrations configured
- [ ] Docker Compose & API Gateway updated
- [ ] All tests passing with required coverage
- [ ] Documentation updated
