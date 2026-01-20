# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Service Documentation**: Complete documentation for Auth, Payment, Notification, Product, Inventory, and Order services.
- **Event Catalog**: Comprehensive catalog of all 13 domain events with JSON schemas and Saga transitions.
- **Runbooks**: Operational guides for Kafka Consumer Lag, Database Connection Pools, and Disaster Recovery.
- **Swagger UI**: Integrated SpringDoc OpenAPI for all microservices (Auth, Payment, Notification, Order, Inventory, Product).
- **CI/CD**: GitHub Actions workflows for Build, Test, and Deployment.
- **License**: Apache 2.0 License.

### Changed
- **Documentation Structure**: Reorganized into `docs/api`, `docs/services`, `docs/architecture` for better navigability.
- **Status Indicators**: Updated all READMEs to reflect the current "Complete" status of services.

### Fixed
- **Missing Docs**: Addressed gaps in Service Documentation (Product, Inventory).
- **Inconsistencies**: Fixed status indicators and cross-references in documentation.

## [0.1.0] - 2026-01-15

### Added
- Initial project structure with Spring Boot microservices.
- Basic API Gateway configuration.
- Docker Compose setup for local development.
