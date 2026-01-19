# Product Service

**Port**: 8081
**Database**: `product_db`
**Technology**: Spring Boot, PostgeSQL
**Pattern**: CQRS (Separated Command/Query logic in Service)

---

## Overview

The Product Service is the catalog authority. It manages product lifecycle (creation, updates, pricing, status) and provides read-optimized APIs for browsing.

### Key Responsibilities

- **Product Lifecycle**: Create, update, activate, discontinue.
- **Catalog Browsing**: List all products, get product details.
- **CQRS**: Separates write operations (commands) from read operations (queries).
- **Concurrency**: Manages product state transitions.

---

## API Endpoints

### Queries

#### Get All Products

```http
GET /api/products
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "prd-123...",
      "name": "Smartphone",
      "price": 699.99,
      "status": "ACTIVE"
    }
  ]
}
```

#### Get Product by ID

```http
GET /api/products/{id}
```

### Commands (Secured)

#### Create Product

```http
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Gaming Laptop",
  "description": "High performance laptop",
  "sku": "GL-001",
  "price": 1299.00
}
```

#### Update Product

```http
PUT /api/products/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Gaming Laptop Pro",
  "description": "Updated description",
  "price": 1199.00
}
```

#### Lifecycle Actions

- **Activate**: `POST /api/products/{id}/activate`
- **Deactivate**: `POST /api/products/{id}/deactivate`
- **Discontinue**: `POST /api/products/{id}/discontinue`
- **Delete**: `DELETE /api/products/{id}`

---

## Domain Model

### Product Entity

The `Product` aggregate root enforces invariants (non-empty name, valid price, status transitions).

**States**:
- `DRAFT`: Newly created.
- `ACTIVE`: Available for sale.
- `INACTIVE`: Temporarily unavailable (hidden).
- `DISCONTINUED`: Permanently removed.

### Database Schema

```sql
CREATE TABLE products (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price_amount DECIMAL(19,4) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

---

## Configuration

### Application Properties

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
  jpa:
    hibernate:
      ddl-auto: update # For dev
```

---

## Running

```bash
# Run locally
./gradlew :product-service:bootRun

# Docker
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/product_db \
  ecommerce/product-service
```
