# Contributing to E-commerce Backend

Thank you for your interest in contributing to our project! 

## Getting Started

1.  **Fork the repository** and create your branch from `develop`.
2.  **Set up your environment**: Follow the [Development Setup Guide](DEVELOPMENT.md).
3.  **Ensure all tests pass**: Run `./gradlew test integrationTest`.

## Coding Standards

- **Language**: Java 21+ with Spring Boot 3.3+.
- **Style**: We follow the **Google Java Style Guide**. 
  - Run `./gradlew checkstyleMain` to verify.
- **Microservices**: Each service should maintain its own domain models and persistence.
- **Events**: Use the [Event Catalog](docs/architecture/event-catalog.md) to define new events.

## Branching Strategy

- `main`: Production-ready code (tags only).
- `develop`: Main integration branch.
- `feature/*`: New features.
- `bugfix/*`: Bug fixes.
- `release/*`: Release candidates.

## Pull Request Process

1.  **Draft PR**: Open a draft PR as soon as you start work to notify the team.
2.  **Description**: Include a clear description of changes, issue link, and testing evidence.
3.  **Review**: At least two approvals are required.
4.  **CI**: All GitHub Actions must pass.

## Documentation Requirements

If you add a new feature:
- Update the relevant [Service Documentation](docs/services/).
- Update the [Event Catalog](docs/architecture/event-catalog.md) if new events are added.
- Add unit and integration tests.

## Security

Please report security vulnerabilities directly to `security@ecommerce.com` instead of opening a public issue.
