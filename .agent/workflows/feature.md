---
description: Feature development workflow for new functionality
---

# Feature Development Workflow

## Overview
This workflow guides the development of new features in the ecommerce-backend microservices.

---

## Steps

### 1. Analyze Requirement
- Understand the user story/requirement thoroughly
- Identify which services will be affected
- Check existing codebase for similar patterns
- Review relevant documentation in `docs/`

### 2. Propose Architecture
- Design domain model changes (entities, value objects, events)
- Define API contracts if creating new endpoints
  - Request/Response DTOs
  - HTTP methods and paths
  - Error responses
- Identify events/messages needed for Kafka
- Determine if Saga pattern is required
- **Identify if Feature Flags are needed**:
  - For risky changes that need a kill-switch
  - For gradual rollout (canary) to subset of users
  - For A/B testing requirements
- Document design in `docs/` folder

### 3. Human Review ⏸️
**STOP HERE - Wait for human approval**
- Present the architecture proposal
- Address feedback before proceeding
- Update design based on review

### 4. Implement Code
- Follow hexagonal architecture structure:
  ```
  application/  → Use cases, services, DTOs
  domain/       → Entities, value objects, domain logic
  infrastructure/ → Repositories, Kafka, external APIs
  ```
- Implementation order: Domain → Application → Infrastructure
- Use Java 21 features:
  - `record` for DTOs
  - `sealed class` for state machines
  - Pattern matching where appropriate
  - Virtual threads for blocking I/O
- **Implement Feature Flags** (if identified in design):
  - Use a centralized configuration or feature flag service
  - Default flags to `off` if risky
- Follow rules in `.agent/rules/`

### 5. Write Tests
- Unit tests for service layer (70% coverage minimum)
- Unit tests for domain logic (80% coverage minimum)
- Integration tests if feature involves cross-service communication
- Use Testcontainers for integration tests
- Follow naming: `given_when_then` or `arrange_act_assert`

### 6. Self Review
- Check code against all rules in `.agent/rules/`:
  - Architecture rules
  - Security rules
  - Code quality rules
  - API rules (if applicable)
  - Messaging rules (if using Kafka)
- Run build: `./gradlew build`
- Ensure all tests pass
- Check JaCoCo coverage report

### 7. Create PR
- Make small, focused commits
- Use clear commit messages
- Reference ticket/issue number
- Fill PR template with:
  - What changed
  - Why it changed
  - How to test

---

## Example Checklist

- [ ] Requirement analyzed
- [ ] Architecture designed and documented
- [ ] Human approval received
- [ ] Domain layer implemented
- [ ] Application layer implemented
- [ ] Infrastructure layer implemented
- [ ] Unit tests written (coverage ≥ 70%)
- [ ] Integration tests written (if needed)
- [ ] All tests passing
- [ ] Code reviewed against rules
- [ ] PR created
