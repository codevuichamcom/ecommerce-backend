# Auth Service

**Port**: 8086
**Database**: `auth_db`
**Security**: JWT (HS256)

---

## Overview

The Auth Service manages user identities, handling registration, login, and token management. It issues JWT Access Tokens and Refresh Tokens to secure the platform.

### Key Responsibilities

- **User Management**: Registration, profile updates.
- **Authentication**: Verifying credentials and issuing tokens.
- **Session Management**: Handling refresh tokens and logout.
- **Role Management**: Assigning permissions (CUSTOMER, ADMIN).

---

## API Endpoints

### Authentication

#### Register

```http
POST /auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "roles": ["CUSTOMER"]
}
```

**Response** (201 Created):
*Empty body*

#### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securePassword123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### Refresh Token

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response** (200 OK):
*Same as Login response (new Access Token, same or rotated Refresh Token)*

#### Logout

```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response** (204 No Content)

---

## Security Architecture

### JWT Implementation

Implemented in `JwtTokenProvider.java`.

- **Algorithm**: HS256 (HMAC SHA-256)
- **Secret**: Min 32 bytes (configured via `jwt.secret`)
- **Access Token Validity**: 1 hour (default)
- **Refresh Token Validity**: 30 days (default)

**Token Payload (Claims)**:
```json
{
  "sub": "user-uuid-123",
  "username": "john.doe",
  "roles": ["CUSTOMER"],
  "iat": 1705600000,
  "exp": 1705603600
}
```

### Refresh Token Flow

1.  Client logs in → receives `accessToken` & `refreshToken`.
2.  `accessToken` expires (401 Unauthorized).
3.  Client calls `/auth/refresh` with `refreshToken`.
4.  Service verifies `refreshToken` in database (whitelist/blacklist check).
5.  Service issues new `accessToken`.

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id VARCHAR(100) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    roles TEXT[] NOT NULL, -- Stored as array string
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);
```

### Refresh Tokens Table

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(512) NOT NULL,
    user_id VARCHAR(100) NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE
);
```

---

## Configuration

### Application Properties

```yaml
server:
  port: 8086

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: validate

jwt:
  secret: ${JWT_SECRET}
  access-token-validity-seconds: 3600 # 1 hour
  refresh-token-validity-seconds: 2592000 # 30 days
```

## Troubleshooting
 
 ### Login Fails (401 Unauthorized)
 - **Check**: Are credentials correct?
 - **Check**: Is the user account `enabled` in database?
 - **Check**: Has the password hashing algorithm changed?
 
 ### Token Issues
 - **Invalid Signature**: Ensure `jwt.secret` matches between Auth Service and Gateway.
 - **Expired**: Check server time synchronization.
 
 ---
 
 ## Running

```bash
# Run locally
./gradlew :auth-service:bootRun

# Docker
docker run -p 8086:8086 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/auth_db \
  ecommerce/auth-service
```
