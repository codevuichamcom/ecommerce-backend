# Contributing to E-commerce Backend

Thank you for your interest in contributing to our project! We welcome contributions from everyone.

## 🚀 Getting Started

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** locally:
    ```bash
    git clone https://github.com/YOUR-USERNAME/ecommerce-backend.git
    cd ecommerce-backend
    ```
3.  **Set up your environment**: Follow the detailed [Development Setup Guide](docs/DEVELOPMENT.md).
4.  **Create a branch** for your changes:
    ```bash
    git checkout -b feature/amazing-feature
    # or
    git checkout -b bugfix/critical-fix
    ```

## 🛠️ Development Workflow

1.  **Code**: Write clean, maintainable code following our standards.
2.  **Test**: Add unit tests for logic and integration tests for APIs.
    - Run all tests: `./gradlew test integrationTest`
3.  **Document**: Update `README.md` and `docs/` if you change behavior.
4.  **Verify**: Ensure the build passes locally: `./gradlew build`

## 📝 Coding Standards

We follow strict coding guidelines to maintain quality:

-   **Language**: Java 21+ with Spring Boot 3.3+.
-   **Style**: Google Java Style Guide.
    -   Auto-format: `./gradlew goJF` (if configured) or use IDE formatter.
    -   Verify: `./gradlew checkstyleMain`.
-   **Architecture**: Domain-Driven Design (DDD).
    -   Keep core logic in `domain` package.
    -   Keep external concerns (DB, Web) in `infrastructure`.
-   **Commits**: Use Conventional Commits.
    -   `feat: add new payment gateway`
    -   `fix: resolve null pointer in user service`
    -   `docs: update api reference`

## 📋 Pull Request Process

1.  **Draft PR**: Open a draft PR early for feedback.
2.  **Description**:
    -   What does this change do?
    -   Why is it needed?
    -   Screenshots (if UI/API output changed).
3.  **Checklist**:
    -   [ ] Tests added/passed?
    -   [ ] Documentation updated?
    -   [ ] Linter/Checkstyle passed?
4.  **Review**: Wait for at least 2 approvals from maintainers.
5.  **Merge**: Squash and merge.

## 🔍 Code Review Checklist

Reviewers will check for:

-   **Correctness**: Does it fix the issue? Are edge cases handled?
-   **Security**: No hardcoded secrets? Input validation present?
-   **Performance**: N+1 queries? Heavy computations on main thread?
-   **Readability**: clear variable names, helpful comments?
-   **Testing**: Do tests cover the changes?

## 🤝 Community

-   **Questions?** Join our Slack channel or open a [Discussion](https://github.com/ecommerce/discussions).
-   **Bugs?** Open an [Issue](https://github.com/ecommerce/issues) with reproduction steps.

Thank you for contributing!
