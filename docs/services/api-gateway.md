# API Gateway

**Port**: 8080
**Technology**: Spring Cloud Gateway
**Security**: JWT Authentication, Rate Limiting

---

## Overview

The API Gateway is the single entry point for all client requests. It handles routing, authentication, and rate limiting before forwarding requests to downstream microservices.

### Key Features

- **Centralized Routing**: Maps external paths to internal service URLs.
- **Authentication**: Validates JWT tokens for secured endpoints.
- **Rate Limiting**: Protects services from abuse using Redis.
- **Health Aggregation**: Exposes health checks for all downstream services.

---

## Routes Configuration

Defined in `RouteConfig.java`.

| Path | Target Service | Rate Limit (Replenish/Burst) | Security |
|------|----------------|------------------------------|----------|
| `/api/products/**` | Product Service (8081) | 50/100 (User) | Public (GET) / Secured (Others) |
| `/api/inventory/**` | Inventory Service (8082) | N/A | Internal / Secured |
| `/api/orders/**` | Order Service (8083) | 50/100 (User) | Secured |
| `/api/payments/**` | Payment Service (8084) | 10/20 (Anon)* | Secured |
| `/api/notifications/**`| Notification Service (8085)| N/A | Internal / Secured |
| `/auth/**` | Auth Service (8086) | 10/20 (Anon) | Public |

*\* Note: Payment service uses anonymous rate limiter in current config (to be reviewed).*

### Health Check Routes

| Path | Forwarded To |
|------|--------------|
| `/health/product` | `http://localhost:8081/actuator/health` |
| `/health/inventory` | `http://localhost:8082/actuator/health` |
| `/health/order` | `http://localhost:8083/actuator/health` |
| `/health/payment` | `http://localhost:8084/actuator/health` |
| `/health/notification`| `http://localhost:8085/actuator/health` |
| `/health/auth` | `http://localhost:8086/actuator/health` |

---

## Security

### JWT Authentication

Implemented in `JwtAuthenticationFilter.java`.

- **Mechanism**: Parses `Authorization: Bearer <token>` header.
- **Validation**: Verifies signature using `jwt.secret` (HS256).
- **Context Propagation**: Forwards user info to downstream services via headers:
    - `X-User-Id`
    - `X-User-Name`
    - `X-User-Roles`

### Public Endpoints

The following endpoints bypass JWT authentication:

- `/auth/login`
- `/auth/register`
- `/auth/refresh`
- `/api/products` (starts with check)
- `/actuator/health`
- `/actuator/prometheus`

---

## Rate Limiting

Implemented using **Redis Rate Limiter** (Token Bucket algorithm).

### Strategies

1.  **User Rate Limiter** (`userKeyResolver`)
    - **Key**: `X-User-Id` header (or IP if missing).
    - **Limit**: 50 requests/second (replenish rate), 100 burst.
    - **Used By**: Product (Authenticated), Order.

2.  **Anonymous/IP Rate Limiter** (`ipKeyResolver` / `userKeyResolver` fallback)
    - **Key**: Client IP Address.
    - **Limit**: 10 requests/second (replenish rate), 20 burst.
    - **Used By**: Auth, Payment.

---

## Configuration

### Application Properties

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ${JWT_SECRET} # Must be >= 32 bytes
```

## Running

```bash
# Run locally
./gradlew :api-gateway:bootRun

# Docker
docker run -p 8080:8080 ecommerce/api-gateway
```
