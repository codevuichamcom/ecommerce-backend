# 🔌 API Documentation

Complete API reference for the E-commerce Backend microservices.

---

## Quick Access

### Swagger UI (Interactive Documentation)

Each service exposes interactive API documentation via Swagger UI:

| Service | Swagger UI | OpenAPI JSON | Port |
|---------|------------|--------------|------|
| **Product Service** | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs | 8081 |
| **Inventory Service** | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs | 8082 |
| **Order Service** | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs | 8083 |
| **Payment Service** | http://localhost:8084/swagger-ui.html ⚠️ | http://localhost:8084/api-docs ⚠️ | 8084 |
| **Notification Service** | http://localhost:8085/swagger-ui.html ⚠️ | http://localhost:8085/api-docs ⚠️ | 8085 |
| **Auth Service** | http://localhost:8086/swagger-ui.html ⚠️ | http://localhost:8086/api-docs ⚠️ | 8086 |

⚠️ = SpringDoc OpenAPI not yet configured (coming soon)

---

## API Design Conventions

### REST Principles

All APIs follow RESTful design principles:

| HTTP Method | Purpose | Idempotent | Safe |
|-------------|---------|------------|------|
| **GET** | Retrieve resource(s) | ✅ | ✅ |
| **POST** | Create new resource | ❌ | ❌ |
| **PUT** | Replace entire resource | ✅ | ❌ |
| **PATCH** | Partial update | ❌ | ❌ |
| **DELETE** | Remove resource | ✅ | ❌ |

### URL Structure

```
https://api.example.com/{service}/api/{version}/{resource}/{id}/{sub-resource}
```

**Examples**:
- `GET /api/v1/products` - List all products
- `GET /api/v1/products/{id}` - Get specific product
- `POST /api/v1/orders` - Create new order
- `GET /api/v1/orders/{id}/items` - Get order items

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| **Resources** | Plural nouns | `/products`, `/orders` |
| **IDs** | ULID/UUID format | `/products/01HQZX3Y4Z5A6B7C8D9E0F1G2H` |
| **Query params** | camelCase | `?sortBy=price&pageSize=20` |
| **JSON fields** | camelCase | `{"productId": "...", "unitPrice": 100}` |

### HTTP Status Codes

#### Success Codes

| Code | Meaning | Usage |
|------|---------|-------|
| **200 OK** | Success | GET, PUT, PATCH successful |
| **201 Created** | Resource created | POST successful |
| **204 No Content** | Success, no body | DELETE successful |

#### Client Error Codes

| Code | Meaning | Usage |
|------|---------|-------|
| **400 Bad Request** | Invalid input | Validation errors |
| **401 Unauthorized** | Not authenticated | Missing/invalid token |
| **403 Forbidden** | Not authorized | Insufficient permissions |
| **404 Not Found** | Resource not found | Invalid ID |
| **409 Conflict** | Resource conflict | Duplicate, optimistic lock failure |
| **422 Unprocessable Entity** | Business rule violation | Insufficient stock |

#### Server Error Codes

| Code | Meaning | Usage |
|------|---------|-------|
| **500 Internal Server Error** | Unexpected error | Unhandled exceptions |
| **503 Service Unavailable** | Service down | Database unavailable |

### Error Response Format

All errors follow **RFC 7807 Problem Details** format:

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Product price must be positive",
  "instance": "/api/v1/products",
  "timestamp": "2026-01-19T10:15:30Z",
  "errors": [
    {
      "field": "price",
      "message": "must be greater than 0",
      "rejectedValue": -10
    }
  ]
}
```

**Fields**:
- `type`: URI reference identifying the problem type
- `title`: Short, human-readable summary
- `status`: HTTP status code
- `detail`: Detailed explanation
- `instance`: URI of the specific occurrence
- `timestamp`: When the error occurred
- `errors`: Array of validation errors (optional)

### Pagination

**Query Parameters**:
```
GET /api/v1/products?page=0&size=20&sort=name,asc
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (0-indexed) |
| `size` | `20` | Items per page |
| `sort` | - | Sort field and direction |

**Response Format**:
```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### Filtering

**Query Parameters**:
```
GET /api/v1/products?status=ACTIVE&minPrice=10&maxPrice=100
```

### Sorting

**Query Parameter**:
```
GET /api/v1/products?sort=price,asc&sort=name,desc
```

### Idempotency

**POST requests** support idempotency via `Idempotency-Key` header:

```http
POST /api/v1/orders
Idempotency-Key: 01HQZX3Y4Z5A6B7C8D9E0F1G2H
Content-Type: application/json

{...}
```

**Behavior**:
- First request: Creates order, returns 201
- Duplicate request (same key): Returns existing order, returns 200
- Key expires after 24 hours

---

## Authentication & Authorization

### JWT Authentication

**Obtain Token**:
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "customer@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Use Token**:
```http
GET /api/v1/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **CUSTOMER** | Create orders, view own orders |
| **ADMIN** | Full access to all resources |

---

## Common Headers

### Request Headers

| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| `Authorization` | ✅ (most endpoints) | JWT bearer token | `Bearer eyJ...` |
| `Content-Type` | ✅ (POST/PUT/PATCH) | Request body format | `application/json` |
| `Idempotency-Key` | ⚠️ (POST only) | Idempotency key | `01HQZ...` |
| `Accept` | ❌ | Response format | `application/json` |

### Response Headers

| Header | Description | Example |
|--------|-------------|---------|
| `Content-Type` | Response format | `application/json` |
| `Location` | Created resource URI | `/api/v1/orders/01HQZ...` |
| `X-Total-Count` | Total items (pagination) | `100` |

---

## API Endpoints by Service

### Product Service (Port 8081)

**Base URL**: `http://localhost:8081/api/v1`

#### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/products` | List all products | ❌ |
| `GET` | `/products/{id}` | Get product by ID | ❌ |
| `POST` | `/products` | Create product | ✅ ADMIN |
| `PUT` | `/products/{id}` | Update product | ✅ ADMIN |
| `DELETE` | `/products/{id}` | Delete product | ✅ ADMIN |

**Example: List Products**
```http
GET /api/v1/products?status=ACTIVE&page=0&size=20
```

**Response**:
```json
{
  "content": [
    {
      "id": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
      "name": "Laptop Pro 15",
      "sku": "LAPTOP-PRO-15",
      "price": 1299.99,
      "currency": "USD",
      "status": "ACTIVE",
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Swagger UI**: http://localhost:8081/swagger-ui.html

---

### Inventory Service (Port 8082)

**Base URL**: `http://localhost:8082/api/v1`

#### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/inventory/{productId}` | Get inventory for product | ❌ |
| `POST` | `/inventory/{productId}/reserve` | Reserve stock | ✅ |
| `POST` | `/inventory/{productId}/release` | Release reserved stock | ✅ |

**Example: Check Inventory**
```http
GET /api/v1/inventory/01HQZX3Y4Z5A6B7C8D9E0F1G2H
```

**Response**:
```json
{
  "productId": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
  "availableQuantity": 50,
  "reservedQuantity": 5,
  "totalQuantity": 55
}
```

**Swagger UI**: http://localhost:8082/swagger-ui.html

---

### Order Service (Port 8083)

**Base URL**: `http://localhost:8083/api/v1`

#### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/orders` | Create order | ✅ |
| `GET` | `/orders/{id}` | Get order by ID | ✅ |
| `GET` | `/orders/customer/{customerId}` | List customer orders | ✅ |
| `POST` | `/orders/{id}/cancel` | Cancel order | ✅ |

**Example: Create Order**
```http
POST /api/v1/orders
Authorization: Bearer eyJ...
Idempotency-Key: 01HQZX3Y4Z5A6B7C8D9E0F1G2H
Content-Type: application/json

{
  "customerId": "customer-123",
  "items": [
    {
      "productId": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
      "quantity": 2
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "id": "01HQZY5Z6A7B8C9D0E1F2G3H4I",
  "customerId": "customer-123",
  "status": "PENDING",
  "items": [
    {
      "productId": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
      "productName": "Laptop Pro 15",
      "quantity": 2,
      "unitPrice": 1299.99,
      "currency": "USD",
      "subtotal": 2599.98
    }
  ],
  "totalAmount": 2599.98,
  "currency": "USD",
  "createdAt": "2026-01-19T10:15:30Z"
}
```

**Swagger UI**: http://localhost:8083/swagger-ui.html

---

### Payment Service (Port 8084)

**Base URL**: `http://localhost:8084/api/v1`

⚠️ **Note**: Swagger UI not yet configured. Coming soon.

#### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `GET` | `/payments/order/{orderId}` | Get payment for order | ✅ |

---

### Notification Service (Port 8085)

**Base URL**: `http://localhost:8085/api/v1`

⚠️ **Note**: Swagger UI not yet configured. Coming soon.

**Event-driven service** - No public REST API. Consumes Kafka events.

---

### Auth Service (Port 8086)

**Base URL**: `http://localhost:8086/api/v1`

⚠️ **Note**: Swagger UI not yet configured. Coming soon.

#### Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/auth/register` | Register new user | ❌ |
| `POST` | `/auth/login` | Login user | ❌ |
| `POST` | `/auth/refresh` | Refresh access token | ❌ |
| `POST` | `/auth/logout` | Logout user | ✅ |

---

## Testing APIs

### Using cURL

**Create Order**:
```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "items": [
      {
        "productId": "01HQZX3Y4Z5A6B7C8D9E0F1G2H",
        "quantity": 2
      }
    ]
  }'
```

### Using HTTPie

**List Products**:
```bash
http GET http://localhost:8081/api/v1/products status==ACTIVE page==0 size==10
```

### Using Postman

1. Import OpenAPI spec: `http://localhost:8081/api-docs`
2. Set environment variables:
   - `BASE_URL`: `http://localhost:8081`
   - `ACCESS_TOKEN`: Your JWT token
3. Use pre-configured requests

---

## API Versioning

### Current Version: v1

All endpoints are prefixed with `/api/v1/`

### Versioning Strategy

- **URL versioning**: `/api/v1/`, `/api/v2/`
- **Backward compatibility**: v1 maintained for 6 months after v2 release
- **Deprecation**: Announced 3 months in advance via response headers

**Deprecation Header**:
```http
HTTP/1.1 200 OK
Deprecation: true
Sunset: Sat, 31 Dec 2026 23:59:59 GMT
Link: </api/v2/products>; rel="successor-version"
```

---

## Rate Limiting

### Limits (Future Implementation)

| Tier | Requests/minute | Burst |
|------|-----------------|-------|
| **Anonymous** | 60 | 10 |
| **Authenticated** | 300 | 50 |
| **Premium** | 1000 | 100 |

### Rate Limit Headers

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 299
X-RateLimit-Reset: 1642598400
```

---

## CORS Configuration

### Allowed Origins

**Development**: `*` (all origins)

**Production**: Specific domains only
```yaml
allowedOrigins:
  - https://app.example.com
  - https://admin.example.com
```

### Allowed Methods

`GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`

### Exposed Headers

`Authorization`, `Content-Type`

---

## API Best Practices

### 1. Use Idempotency Keys

Always include `Idempotency-Key` header for POST requests:

```http
POST /api/v1/orders
Idempotency-Key: 01HQZX3Y4Z5A6B7C8D9E0F1G2H
```

### 2. Handle Errors Gracefully

Check HTTP status code and parse error response:

```javascript
if (response.status === 400) {
  const error = await response.json();
  console.error(error.detail);
  error.errors.forEach(e => {
    console.error(`${e.field}: ${e.message}`);
  });
}
```

### 3. Use Pagination

Always paginate large result sets:

```http
GET /api/v1/products?page=0&size=20
```

### 4. Implement Retries

Retry failed requests with exponential backoff:

```javascript
async function fetchWithRetry(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fetch(url, options);
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await sleep(Math.pow(2, i) * 1000);
    }
  }
}
```

### 5. Cache Responses

Cache GET responses when appropriate:

```http
GET /api/v1/products/01HQZX3Y4Z5A6B7C8D9E0F1G2H
Cache-Control: max-age=600
```

---

## OpenAPI Specifications

### Download Specs

| Service | OpenAPI 3.0 JSON |
|---------|------------------|
| Product | http://localhost:8081/api-docs |
| Inventory | http://localhost:8082/api-docs |
| Order | http://localhost:8083/api-docs |

### Generate Client SDKs

Using OpenAPI Generator:

```bash
# Download spec
curl http://localhost:8081/api-docs > product-api.json

# Generate TypeScript client
openapi-generator-cli generate \
  -i product-api.json \
  -g typescript-axios \
  -o ./clients/product-client
```

---

## API Monitoring

### Health Checks

All services expose health endpoints:

```http
GET /actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "kafka": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### Metrics

Prometheus metrics available at:

```http
GET /actuator/prometheus
```

---

## Next Steps

- [Development Guide](../DEVELOPMENT.md) - Local API testing
- [Authentication Guide](../SECURITY.md) - JWT authentication
- [Data Model](../architecture/data-model.md) - Database schemas
- [Troubleshooting](../TROUBLESHOOTING.md) *(Coming Soon)* - Common API errors

---

## Support

- **Swagger UI Issues**: Check service is running on correct port
- **Authentication Errors**: Verify JWT token is valid and not expired
- **API Questions**: Ask in `#ecommerce-backend` Slack channel

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-19  
**Maintained By**: Backend Team
