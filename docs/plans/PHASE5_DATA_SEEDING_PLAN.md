# Data Seeding Implementation Details

> **Comprehensive step-by-step guide for implementing data seeding in the Ecommerce Backend**

This document provides detailed implementation instructions for each phase of the data seeding strategy. Follow these steps sequentially for successful implementation.

---

## Table of Contents
1. [Phase 1: Project Setup & Dependencies](#phase-1-project-setup--dependencies)
2. [Phase 2: Reference Data Seeding](#phase-2-reference-data-seeding)
3. [Phase 3: Development Data Seeders](#phase-3-development-data-seeders)
4. [Phase 4: Configuration & Profile Setup](#phase-4-configuration--profile-setup)
5. [Phase 5: Seeding Execution Scripts](#phase-5-seeding-execution-scripts)
6. [Phase 6: Documentation](#phase-6-documentation)
7. [Phase 7: Testing & Validation](#phase-7-testing--validation)
8. [Phase 8: Optional Enhancements](#phase-8-optional-enhancements)

---

## Phase 1: Project Setup & Dependencies

### Estimated Time: 15 minutes

### Step 1.1: Add DataFaker Dependency

**Location:** `ecommerce-backend/build.gradle.kts`

1. Open the root `build.gradle.kts` file
2. Locate the `subprojects` block, specifically the `dependencies` section (around line 54)
3. Add the DataFaker dependency:

```kotlin
// Common dependencies for all subprojects
dependencies {
    // Lombok
    "compileOnly"("org.projectlombok:lombok")
    "annotationProcessor"("org.projectlombok:lombok")
    "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
    "testCompileOnly"("org.projectlombok:lombok")
    "testAnnotationProcessor"("org.projectlombok:lombok")

    // Testing
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.assertj:assertj-core")

    // Observability
    "implementation"("io.micrometer:micrometer-observation")
    "implementation"("io.micrometer:micrometer-tracing-bridge-brave")
    "implementation"("io.zipkin.reporter2:zipkin-reporter-brave")
    "implementation"("io.micrometer:micrometer-registry-prometheus")
    "implementation"("org.springframework.boot:spring-boot-starter-actuator")

    // ⭐ ADD THIS LINE - DataFaker for realistic data generation
    "implementation"("net.datafaker:datafaker:2.1.0")
}
```

### Step 1.2: Verify Dependency Installation

**Command:**
```bash
cd ecommerce-backend
./gradlew dependencies --configuration runtimeClasspath | grep datafaker
```

**Expected Output:**
```
+--- net.datafaker:datafaker:2.1.0
```

**Windows:**
```batch
cd ecommerce-backend
gradlew.bat dependencies --configuration runtimeClasspath | findstr datafaker
```

### Step 1.3: Clean and Build

**Command:**
```bash
./gradlew clean build
```

**Expected Output:**
```
BUILD SUCCESSFUL in 45s
```

### Verification Checklist
- [ ] DataFaker dependency added to `build.gradle.kts`
- [ ] Dependency appears in `./gradlew dependencies` output
- [ ] Clean build completes successfully
- [ ] No compilation errors

### Troubleshooting

**Issue:** `Could not find net.datafaker:datafaker:2.1.0`
- **Solution:** Ensure `mavenCentral()` is in repositories block
- **Solution:** Try `./gradlew --refresh-dependencies`

**Issue:** Build fails with "dependency resolution failed"
- **Solution:** Clear Gradle cache: `rm -rf ~/.gradle/caches/`
- **Solution:** Run `./gradlew clean build --refresh-dependencies`

---

## Phase 2: Reference Data Seeding (Flyway Migrations)

### Estimated Time: 30 minutes

### Step 2.1: Create Auth Service System Users Migration

**Location:** `auth-service/src/main/resources/db/migration/V10__seed_system_users.sql`

1. Navigate to auth-service migrations directory:
```bash
cd ecommerce-backend/auth-service/src/main/resources/db/migration
```

2. Create the file `V10__seed_system_users.sql`:
```bash
touch V10__seed_system_users.sql
```

3. Add the following SQL content:

```sql
-- =============================================================================
-- Flyway Migration V10: Seed System Users
-- Description: Creates admin and service account users for the ecommerce system
-- Author: Development Team
-- Date: 2024-01-XX
-- =============================================================================

-- Create admin user
-- Password: admin123 (bcrypt hashed)
-- Role: ADMIN
INSERT INTO users (id, username, email, password_hash, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@ecommerce.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;  -- Idempotent: skip if already exists

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role = 'ADMIN'
);

-- Create service account for inter-service communication
-- Password: admin123 (same hash for simplicity)
-- Role: SERVICE
INSERT INTO users (id, username, email, password_hash, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'service-account',
    'service@ecommerce.internal',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Assign SERVICE role to service account
INSERT INTO user_roles (user_id, role)
SELECT id, 'SERVICE'
FROM users
WHERE username = 'service-account'
AND NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'service-account')
    AND role = 'SERVICE'
);

-- Verification: Count system users
-- Expected: 2 users (admin + service-account)
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users
    WHERE username IN ('admin', 'service-account');

    RAISE NOTICE 'System users created: %', user_count;

    IF user_count < 2 THEN
        RAISE EXCEPTION 'System users not created properly. Expected 2, got %', user_count;
    END IF;
END $$;
```

### Step 2.2: Verify Migration File Naming

**Important:** Flyway migrations must follow naming convention: `V{version}__{description}.sql`

Check your migration:
```bash
ls -la auth-service/src/main/resources/db/migration/V10__*
```

**Expected output:**
```
-rw-r--r-- 1 user user 2048 Jan 30 10:00 V10__seed_system_users.sql
```

### Step 2.3: Test Migration Locally

1. Ensure PostgreSQL is running:
```bash
docker-compose up -d postgres
```

2. Start auth-service to run migration:
```bash
cd auth-service
./gradlew bootRun
```

3. Check logs for successful migration:
```
Flyway: Migrating schema "auth_db" to version "10 - seed system users"
Flyway: Successfully applied 1 migration to schema "auth_db"
```

### Step 2.4: Verify System Users in Database

Connect to database and verify:

```bash
docker exec -it ecommerce-postgres psql -U postgres -d auth_db
```

```sql
-- Check users
SELECT username, email, enabled FROM users WHERE username IN ('admin', 'service-account');

-- Expected output:
--    username       |           email            | enabled
-- ------------------+---------------------------+---------
--  admin            | admin@ecommerce.com        | t
--  service-account  | service@ecommerce.internal | t

-- Check roles
SELECT u.username, r.role
FROM users u
JOIN user_roles r ON u.id = r.user_id
WHERE u.username IN ('admin', 'service-account');

-- Expected output:
--    username       | role
-- ------------------+---------
--  admin            | ADMIN
--  service-account  | SERVICE

\q  -- Exit psql
```

### Verification Checklist
- [ ] Migration file `V10__seed_system_users.sql` created
- [ ] File follows naming convention `V{version}__{description}.sql`
- [ ] Migration runs successfully (check logs)
- [ ] 2 system users exist in database
- [ ] Admin has ADMIN role
- [ ] Service account has SERVICE role

### Troubleshooting

**Issue:** `FlywayException: Validate failed: Migration checksum mismatch`
- **Solution:** If you edited the file after running, clean and re-run:
```bash
docker-compose down -v  # Drop database
docker-compose up -d postgres
./gradlew bootRun  # Re-run migration
```

**Issue:** `Duplicate key value violates unique constraint`
- **Solution:** Migration already ran. Use `ON CONFLICT DO NOTHING` (already in code above)

**Issue:** Admin user can't login
- **Solution:** Verify password hash is correct: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- **Solution:** Test with bcrypt online tool: password = `admin123`

---

## Phase 3: Development Data Seeders

### Estimated Time: 3-4 hours (all services)

This phase creates data seeders for each service. We'll implement in order:
1. Product Service (no dependencies)
2. Auth Service (no dependencies)
3. Inventory Service (depends on Product)
4. Order Service (depends on Product + Auth)

---

### Phase 3.1: Product Service Seeding

**Estimated Time:** 45 minutes

#### Step 3.1.1: Create Package Structure

```bash
cd ecommerce-backend/product-service/src/main/java/com/ecommerce/product/infrastructure

# Create directories
mkdir -p seed/config
mkdir -p seed/factory
mkdir -p seed/initializer

# Verify structure
tree seed
# Expected:
# seed/
# ├── config/
# ├── factory/
# └── initializer/
```

#### Step 3.1.2: Create DataSeedingConfig

**File:** `product-service/src/main/java/com/ecommerce/product/infrastructure/seed/config/DataSeedingConfig.java`

**Command:**
```bash
cat > seed/config/DataSeedingConfig.java << 'EOF'
package com.ecommerce.product.infrastructure.seed.config;

import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Locale;
import java.util.Random;

/**
 * Configuration for data seeding in development environment.
 * Only active when 'dev' profile is enabled.
 */
@Configuration
@Profile("dev")
public class DataSeedingConfig {

    /**
     * Provides Faker instance with fixed seed for reproducible data.
     * Seed value: 42 (ensures same data generated every time)
     */
    @Bean
    public Faker faker() {
        return new Faker(Locale.US, new Random(42));
    }
}
EOF
```

**Verify file created:**
```bash
ls -la seed/config/DataSeedingConfig.java
```

#### Step 3.1.3: Create ProductDataFactory

**File:** `product-service/src/main/java/com/ecommerce/product/infrastructure/seed/factory/ProductDataFactory.java`

**Copy the complete ProductDataFactory code from the Master Plan (lines 154-296)**

**Key points in ProductDataFactory:**
- `createProduct()`: Creates single product with category-specific data
- `generateProducts()`: Generates 120 products with status distribution
  - 80% ACTIVE (96 products)
  - 10% INACTIVE (12 products)
  - 5% DRAFT (6 products)
  - 5% DISCONTINUED (6 products)
- Category-aware pricing:
  - Electronics: $50-$2000
  - Clothing: $20-$200
  - Books: $10-$50
  - etc.

**Verification:**
```bash
# Compile to check for errors
cd ecommerce-backend/product-service
./gradlew compileJava

# Expected output:
# BUILD SUCCESSFUL
```

#### Step 3.1.4: Create SeededData Container

**File:** `product-service/src/main/java/com/ecommerce/product/infrastructure/seed/factory/SeededData.java`

**Copy code from Master Plan (lines 304-329)**

#### Step 3.1.5: Create DevDataInitializer

**File:** `product-service/src/main/java/com/ecommerce/product/infrastructure/seed/initializer/DevDataInitializer.java`

**Copy code from Master Plan (lines 336-388)**

**Key features:**
- `@Profile("dev")`: Only runs in dev environment
- `ApplicationRunner`: Runs after Spring context is ready
- `@Transactional`: All insertions in single transaction
- Idempotent check: `if (productRepository.count() > 0)`

#### Step 3.1.6: Test Product Service Seeding

**Command:**
```bash
cd ecommerce-backend/product-service

# Start with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Expected Log Output:**
```
INFO  c.e.p.i.s.i.DevDataInitializer - Starting product data seeding...
INFO  c.e.p.i.s.i.DevDataInitializer - Seeded 120 products successfully
INFO  c.e.p.i.s.i.DevDataInitializer -   - Active products: 96
INFO  c.e.p.i.s.i.DevDataInitializer -   - Sample product: Ergonomic Granite Chair (HOM-00000-1234)
```

**Verify in Database:**
```bash
docker exec -it ecommerce-postgres psql -U postgres -d product_db

SELECT COUNT(*) FROM products;
-- Expected: 120

SELECT status, COUNT(*) FROM products GROUP BY status;
-- Expected:
--   status      | count
-- --------------+-------
--  ACTIVE       |    96
--  INACTIVE     |    12
--  DRAFT        |     6
--  DISCONTINUED |     6

SELECT name, sku, price FROM products LIMIT 3;
-- Expected: Realistic product names and prices

\q
```

#### Step 3.1.7: Test Idempotency

Restart the service to verify it doesn't duplicate data:

```bash
# Stop service (Ctrl+C)
# Start again
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Expected Log:**
```
INFO  c.e.p.i.s.i.DevDataInitializer - Products already exist, skipping seeding
```

**Verify count unchanged:**
```bash
docker exec -it ecommerce-postgres psql -U postgres -d product_db -c "SELECT COUNT(*) FROM products;"
-- Expected: 120 (not 240!)
```

### Verification Checklist - Product Service
- [ ] All 4 files created (Config, Factory, SeededData, Initializer)
- [ ] Code compiles without errors
- [ ] Service starts with dev profile
- [ ] Logs show "Seeded 120 products successfully"
- [ ] Database has exactly 120 products
- [ ] Status distribution is correct (96/12/6/6)
- [ ] Products have realistic names, SKUs, prices
- [ ] Restart doesn't duplicate data (idempotent)

---

### Phase 3.2: Auth Service Seeding

**Estimated Time:** 45 minutes

Follow similar steps as Product Service:

#### Step 3.2.1: Create Package Structure

```bash
cd ecommerce-backend/auth-service/src/main/java/com/ecommerce/auth/infrastructure

mkdir -p seed/config
mkdir -p seed/factory
mkdir -p seed/initializer
```

#### Step 3.2.2: Create Files

Create these 4 files (copy from Master Plan):
1. `DataSeedingConfig.java` (lines 400-426)
2. `UserDataFactory.java` (lines 434-519)
3. `SeededData.java` (lines 527-547)
4. `DevDataInitializer.java` (lines 555-607)

**Key features in UserDataFactory:**
- Generates realistic first/last names
- Username format: `{firstname}.{lastname}{index}`
- Email format: `{firstname}.{lastname}{index}@{domain}`
- Password: `password123` (bcrypt hashed)
- 50% users have `lastLoginAt` (random 0-30 days ago)
- 5% users are disabled

#### Step 3.2.3: Test Auth Service Seeding

```bash
cd ecommerce-backend/auth-service
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Expected Log Output:**
```
INFO  c.e.a.i.s.i.DevDataInitializer - Starting user data seeding...
INFO  c.e.a.i.s.i.DevDataInitializer - Seeded 120 customer users successfully
INFO  c.e.a.i.s.i.DevDataInitializer -   - Default password for all: password123
INFO  c.e.a.i.s.i.DevDataInitializer -   - Sample user: john.doe0 (john.doe0@gmail.com)
INFO  c.e.a.i.s.i.DevDataInitializer -   - Enabled: 114, Disabled: 6
```

**Verify in Database:**
```bash
docker exec -it ecommerce-postgres psql -U postgres -d auth_db

-- Should have 122 total users (120 customers + 2 system users)
SELECT COUNT(*) FROM users;
-- Expected: 122

-- Check customer users only
SELECT COUNT(*) FROM user_roles WHERE role = 'CUSTOMER';
-- Expected: 120

-- Check sample users
SELECT username, email, enabled FROM users WHERE username LIKE '%.%' LIMIT 5;

\q
```

### Verification Checklist - Auth Service
- [ ] All 4 files created
- [ ] Service starts with dev profile
- [ ] Logs show "Seeded 120 customer users"
- [ ] Database has 122 total users (120 + 2 system)
- [ ] All customers have CUSTOMER role
- [ ] Sample usernames/emails look realistic
- [ ] Password `password123` works for login

---

### Phase 3.3: Inventory Service Seeding

**Estimated Time:** 30 minutes

**⚠️ Important:** Inventory service depends on Product service. Product service must be running and seeded first!

#### Step 3.3.1: Create Package Structure

```bash
cd ecommerce-backend/inventory-service/src/main/java/com/ecommerce/inventory/infrastructure

mkdir -p seed/config
mkdir -p seed/factory
mkdir -p seed/initializer
```

#### Step 3.3.2: Create Files

Create these 3 files (copy from Master Plan):
1. `DataSeedingConfig.java` (lines 617-643)
   - Includes `RestTemplate` bean for calling Product Service
2. `InventoryDataFactory.java` (lines 651-699)
3. `DevDataInitializer.java` (lines 707-814)

**Key features in DevDataInitializer:**
- Fetches product IDs from Product Service API
- Creates inventory for each product
- Stock distribution:
  - 70% in stock (10-500 units)
  - 20% low stock (1-9 units)
  - 10% out of stock (0 units)
- Adds random reserved quantity (0-5)

#### Step 3.3.3: Add Product Service URL to application.yml

**File:** `inventory-service/src/main/resources/application.yml`

Add this configuration:
```yaml
# Product Service integration
product:
  service:
    url: http://localhost:8081
```

#### Step 3.3.4: Test Inventory Service Seeding

**Pre-requisite:** Product service must be running!

```bash
# Terminal 1: Start Product Service
cd ecommerce-backend/product-service
./gradlew bootRun --args='--spring.profiles.active=dev'

# Terminal 2: Start Inventory Service
cd ecommerce-backend/inventory-service
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Expected Log Output:**
```
INFO  c.e.i.i.s.i.DevDataInitializer - Starting inventory data seeding...
INFO  c.e.i.i.s.i.DevDataInitializer - Found 120 products, creating inventory records...
INFO  c.e.i.i.s.i.DevDataInitializer - Seeded 120 inventory records successfully
INFO  c.e.i.i.s.i.DevDataInitializer -   - In stock: 84
INFO  c.e.i.i.s.i.DevDataInitializer -   - Low stock: 24
INFO  c.e.i.i.s.i.DevDataInitializer -   - Out of stock: 12
```

**Verify in Database:**
```bash
docker exec -it ecommerce-postgres psql -U postgres -d inventory_db

SELECT COUNT(*) FROM inventory;
-- Expected: 120

-- Check stock distribution
SELECT
    CASE
        WHEN available_quantity >= 10 THEN 'In Stock'
        WHEN available_quantity > 0 THEN 'Low Stock'
        ELSE 'Out of Stock'
    END AS stock_status,
    COUNT(*)
FROM inventory
GROUP BY stock_status;

-- Verify all product IDs exist
SELECT COUNT(DISTINCT product_id) FROM inventory;
-- Expected: 120

\q
```

#### Troubleshooting - Inventory Service

**Issue:** "No products found in product service"
- **Solution:** Ensure product-service is running on port 8081
- **Solution:** Check `product.service.url` configuration
- **Solution:** Verify product-service seeded data: `curl http://localhost:8081/api/products?size=1`

**Issue:** Connection refused to product-service
- **Solution:** Wait 15-30 seconds after starting product-service
- **Solution:** Check product-service logs for "Started ProductApplication"

### Verification Checklist - Inventory Service
- [ ] All 3 files created
- [ ] Product service is running and seeded
- [ ] Inventory service starts with dev profile
- [ ] Logs show "Found 120 products"
- [ ] Logs show "Seeded 120 inventory records"
- [ ] Database has 120 inventory records
- [ ] Stock distribution matches expectations (~70/20/10)
- [ ] All product_id values exist in product service

---

### Phase 3.4: Order Service Seeding

**Estimated Time:** 60 minutes

**⚠️ Important:** Order service depends on both Product service AND Auth service!

#### Step 3.4.1: Create Package Structure

```bash
cd ecommerce-backend/order-service/src/main/java/com/ecommerce/order/infrastructure

mkdir -p seed/config
mkdir -p seed/factory
mkdir -p seed/initializer
```

#### Step 3.4.2: Create Files

Create these 3 files (copy from Master Plan):
1. `DataSeedingConfig.java` (lines 826-852)
2. `OrderDataFactory.java` (lines 860-985)
3. `DevDataInitializer.java` (lines 993-1123)

**Key features in OrderDataFactory:**
- Creates orders with 1-5 items each
- Status distribution:
  - 40% Delivered (60 orders)
  - 20% Shipped (30 orders)
  - 15% Paid (22-23 orders)
  - 10% Confirmed (15 orders)
  - 10% Pending (15 orders)
  - 5% Cancelled (7-8 orders)
- Realistic order dates based on status
- Cancelled orders have cancellation reasons

#### Step 3.4.3: Add Service URLs to application.yml

**File:** `order-service/src/main/resources/application.yml`

Add these configurations:
```yaml
# Service integration
product:
  service:
    url: http://localhost:8081

auth:
  service:
    url: http://localhost:8086
```

#### Step 3.4.4: Test Order Service Seeding

**Pre-requisite:** Both Product service AND Auth service must be running!

```bash
# Terminal 1: Product Service
cd ecommerce-backend/product-service
./gradlew bootRun --args='--spring.profiles.active=dev'

# Terminal 2: Auth Service
cd ecommerce-backend/auth-service
./gradlew bootRun --args='--spring.profiles.active=dev'

# Terminal 3: Order Service
cd ecommerce-backend/order-service
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Expected Log Output:**
```
INFO  c.e.o.i.s.i.DevDataInitializer - Starting order data seeding...
INFO  c.e.o.i.s.i.DevDataInitializer - Found 96 products and 120 customers, creating orders...
INFO  c.e.o.i.s.i.DevDataInitializer - Seeded 150 orders successfully
INFO  c.e.o.i.s.i.DevDataInitializer - Order status distribution:
INFO  c.e.o.i.s.i.DevDataInitializer -   - Delivered: 60
INFO  c.e.o.i.s.i.DevDataInitializer -   - Shipped: 30
INFO  c.e.o.i.s.i.DevDataInitializer -   - Paid: 22
INFO  c.e.o.i.s.i.DevDataInitializer -   - Confirmed: 15
INFO  c.e.o.i.s.i.DevDataInitializer -   - Pending: 15
INFO  c.e.o.i.s.i.DevDataInitializer -   - Cancelled: 8
```

**Verify in Database:**
```bash
docker exec -it ecommerce-postgres psql -U postgres -d order_db

-- Check order count
SELECT COUNT(*) FROM orders;
-- Expected: 150

-- Check status distribution
SELECT status, COUNT(*) FROM orders GROUP BY status ORDER BY COUNT(*) DESC;

-- Check order items
SELECT COUNT(*) FROM order_items;
-- Expected: 300-750 items (avg 2-5 items per order)

-- Verify data integrity
-- All orders have items
SELECT COUNT(*) FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE oi.id IS NULL;
-- Expected: 0

-- All order items reference valid products
SELECT COUNT(*) FROM order_items
WHERE product_id NOT LIKE 'prd_%';
-- Expected: 0

-- Check cancelled orders have reasons
SELECT COUNT(*) FROM orders
WHERE status LIKE '%Cancelled%' AND cancel_reason IS NULL;
-- Expected: 0

\q
```

### Verification Checklist - Order Service
- [ ] All 3 files created
- [ ] Product and Auth services are running
- [ ] Order service starts with dev profile
- [ ] Logs show "Found X products and Y customers"
- [ ] Logs show "Seeded 150 orders successfully"
- [ ] Database has 150 orders
- [ ] Status distribution matches expectations
- [ ] All orders have order items
- [ ] All product_id references are valid
- [ ] Cancelled orders have cancel_reason

---

## Phase 4: Configuration & Profile Setup

### Estimated Time: 20 minutes

### Step 4.1: Create application-dev.yml for Each Service

#### Product Service

**File:** `product-service/src/main/resources/application-dev.yml`

```yaml
# Development Profile Configuration
# Used for local development with seed data

spring:
  jpa:
    # Show SQL queries in console (helpful for debugging)
    show-sql: true
    properties:
      hibernate:
        # Format SQL for better readability
        format_sql: true
        # Highlight SQL syntax
        highlight_sql: true
    # Validate schema against entities (don't auto-create)
    hibernate:
      ddl-auto: validate

  flyway:
    # Enable Flyway migrations
    enabled: true
    # Create baseline if needed
    baseline-on-migrate: true

# Logging configuration
logging:
  level:
    # Debug level for our application
    com.ecommerce: DEBUG
    # Show SQL queries
    org.hibernate.SQL: DEBUG
    # Show SQL parameter values
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    # DataFaker logging
    net.datafaker: INFO
```

#### Auth Service

**File:** `auth-service/src/main/resources/application-dev.yml`

```yaml
# Development Profile Configuration
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        highlight_sql: true
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    baseline-on-migrate: true

logging:
  level:
    com.ecommerce: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.security: DEBUG  # Security debugging
    net.datafaker: INFO
```

#### Inventory Service

**File:** `inventory-service/src/main/resources/application-dev.yml`

```yaml
# Development Profile Configuration
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        highlight_sql: true
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    baseline-on-migrate: true

# Product Service integration
product:
  service:
    url: http://localhost:8081

logging:
  level:
    com.ecommerce: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    net.datafaker: INFO
```

#### Order Service

**File:** `order-service/src/main/resources/application-dev.yml`

```yaml
# Development Profile Configuration
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        highlight_sql: true
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    baseline-on-migrate: true

# Service integration
product:
  service:
    url: http://localhost:8081

auth:
  service:
    url: http://localhost:8086

logging:
  level:
    com.ecommerce: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    net.datafaker: INFO
```

### Step 4.2: Verify Profile Configuration

Test each service with dev profile:

```bash
# Product Service
cd product-service
./gradlew bootRun --args='--spring.profiles.active=dev'
# Look for: "The following profiles are active: dev"

# Auth Service
cd auth-service
./gradlew bootRun --args='--spring.profiles.active=dev'

# etc.
```

### Verification Checklist
- [ ] All 4 `application-dev.yml` files created
- [ ] Each service logs "active profiles: dev"
- [ ] SQL queries are visible in console (show-sql: true)
- [ ] Debug logs appear for com.ecommerce package

---

## Phase 5: Seeding Execution Scripts

### Estimated Time: 30 minutes

### Step 5.1: Create Startup Script (Linux/Mac)

**File:** `ecommerce-backend/scripts/seed-dev-environment.sh`

Copy the bash script from Master Plan (lines 1185-1251)

**Make executable:**
```bash
chmod +x scripts/seed-dev-environment.sh
```

### Step 5.2: Create Startup Script (Windows)

**File:** `ecommerce-backend/scripts/seed-dev-environment.bat`

Copy the batch script from Master Plan (lines 1257-1299)

### Step 5.3: Create Database Reset Script

**File:** `ecommerce-backend/scripts/reset-dev-database.sh`

Copy the script from Master Plan (lines 1305-1313)

**Make executable:**
```bash
chmod +x scripts/reset-dev-database.sh
```

### Step 5.4: Test Startup Script

**Linux/Mac:**
```bash
cd ecommerce-backend
./scripts/seed-dev-environment.sh
```

**Windows:**
```batch
cd ecommerce-backend
scripts\seed-dev-environment.bat
```

**Expected behavior:**
1. Databases start (PostgreSQL, Redis, Kafka)
2. Product Service starts → Seeds 120 products
3. Auth Service starts → Seeds 120 users
4. Inventory Service starts → Seeds 120 inventory records
5. Order Service starts → Seeds 150 orders

**Wait times:**
- After Product: 15 seconds
- After Auth: 15 seconds
- After Inventory: 10 seconds

### Verification Checklist
- [ ] Scripts created and executable
- [ ] Linux script runs all services in order
- [ ] Windows script opens 4 separate terminal windows
- [ ] All services seed data successfully
- [ ] No "connection refused" errors

### Troubleshooting

**Issue:** Services start too fast, can't connect
- **Solution:** Increase sleep times in script (15s → 30s)

**Issue:** Port already in use
- **Solution:** Kill existing processes:
```bash
lsof -ti:8081,8082,8083,8086 | xargs kill -9
```

---

## Phase 6: Documentation

### Estimated Time: 30 minutes

### Step 6.1: Create DATA_SEEDING.md

**File:** `ecommerce-backend/docs/DATA_SEEDING.md`

Copy the markdown content from Master Plan (lines 1325-1432)

### Step 6.2: Update Main README.md

**File:** `ecommerce-backend/README.md`

Add this section (find appropriate location):

```markdown
## Development Data Seeding

For local development and testing, the project includes automatic data seeding with realistic data powered by DataFaker.

### Generated Data

| Service | Entity | Count | Details |
|---------|--------|-------|---------|
| Product | Products | 120 | 80% ACTIVE, 10% INACTIVE, 5% DRAFT, 5% DISCONTINUED |
| Auth | Users | 120 | All with role CUSTOMER, password: `password123` |
| Auth | System Users | 2 | admin (ADMIN), service-account (SERVICE) |
| Inventory | Inventory | 120 | Matches products, realistic stock levels |
| Order | Orders | 150 | Distributed across statuses and customers |

### Quick Start with Seeded Data

**Linux/Mac:**
```bash
./scripts/seed-dev-environment.sh
```

**Windows:**
```batch
scripts\seed-dev-environment.bat
```

**Manual Start:**
```bash
# 1. Start databases
docker-compose up -d postgres redis kafka

# 2. Start services in order with dev profile
cd product-service && ./gradlew bootRun --args='--spring.profiles.active=dev'
cd auth-service && ./gradlew bootRun --args='--spring.profiles.active=dev'
cd inventory-service && ./gradlew bootRun --args='--spring.profiles.active=dev'
cd order-service && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Test Credentials

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Customer Users:**
- Any customer: `{firstname}.{lastname}{index}` (e.g., `john.doe0`)
- Password: `password123`

For complete documentation, see [docs/DATA_SEEDING.md](docs/DATA_SEEDING.md).
```

### Verification Checklist
- [ ] DATA_SEEDING.md created with all sections
- [ ] README.md updated with seeding section
- [ ] Documentation is clear and accurate
- [ ] All URLs and commands are correct

---

## Phase 7: Testing & Validation

### Estimated Time: 45 minutes

### Step 7.1: Manual API Verification

Use these curl commands to verify seeded data:

#### Product Service (Port 8081)

```bash
# Get products count
curl http://localhost:8081/api/products?size=1 | jq '.totalElements'
# Expected: 120

# Get products by status
curl http://localhost:8081/api/products?status=ACTIVE&size=1 | jq '.totalElements'
# Expected: ~96

# Get sample product
curl http://localhost:8081/api/products?size=1 | jq '.content[0]' | jq '{name, sku, price, status}'
# Expected: Realistic product data
```

#### Auth Service (Port 8086)

```bash
# Test admin login
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq '.'
# Expected: JWT token

# Test customer login
curl -X POST http://localhost:8086/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe0","password":"password123"}' | jq '.'
# Expected: JWT token

# Get users count (requires auth)
curl http://localhost:8086/api/users?size=1 \
  -H "Authorization: Bearer YOUR_TOKEN" | jq '.totalElements'
# Expected: 122
```

#### Inventory Service (Port 8082)

```bash
# Get inventory count
curl http://localhost:8082/api/inventory?size=1 | jq '.totalElements'
# Expected: 120

# Check stock levels distribution
curl http://localhost:8082/api/inventory?size=200 | \
  jq '[.content[] | select(.availableQuantity >= 10)] | length'
# Expected: ~84 (70%)
```

#### Order Service (Port 8083)

```bash
# Get orders count
curl http://localhost:8083/api/orders?size=1 | jq '.totalElements'
# Expected: 150

# Get order with items
curl http://localhost:8083/api/orders/ord_0 | jq '{id, customerId, status, totalAmount, items: .items | length}'
# Expected: Order with 1-5 items

# Check cancelled orders
curl "http://localhost:8083/api/orders?status=Cancelled&size=10" | jq '.content[] | {id, cancelReason}'
# Expected: All have cancel reasons
```

### Step 7.2: Database Integrity Checks

Run these SQL queries to verify data integrity:

```bash
docker exec -it ecommerce-postgres psql -U postgres
```

```sql
-- Switch to each database and check

\c product_db
SELECT COUNT(*) as product_count FROM products;
SELECT status, COUNT(*) FROM products GROUP BY status;

\c auth_db
SELECT COUNT(*) as user_count FROM users;
SELECT r.role, COUNT(*) FROM user_roles r GROUP BY r.role;

\c inventory_db
SELECT COUNT(*) as inventory_count FROM inventory;
-- Check for orphan inventory (product not in product service)
-- This requires cross-database query or manual verification

\c order_db
SELECT COUNT(*) as order_count FROM orders;
SELECT COUNT(*) as order_item_count FROM order_items;
-- Check all orders have items
SELECT COUNT(*) FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE oi.id IS NULL;
-- Expected: 0
```

### Step 7.3: Idempotency Test

Verify seeding doesn't duplicate data on restart:

```bash
# 1. Check current counts
curl http://localhost:8081/api/products?size=1 | jq '.totalElements'
# Note the count (should be 120)

# 2. Restart product service
# Kill and restart: ./gradlew bootRun --args='--spring.profiles.active=dev'

# 3. Check count again
curl http://localhost:8081/api/products?size=1 | jq '.totalElements'
# Should still be 120 (not 240!)

# Repeat for other services
```

### Step 7.4: Performance Test

Check seeding performance:

```bash
# Time the seeding process
time ./gradlew bootRun --args='--spring.profiles.active=dev'

# Expected:
# Product Service: 3-5 seconds for seeding
# Auth Service: 5-10 seconds for seeding (bcrypt is slow)
# Inventory Service: 2-3 seconds
# Order Service: 5-10 seconds
```

### Master Validation Checklist

**Product Service:**
- [ ] Returns exactly 120 products
- [ ] Status distribution: ~96 ACTIVE, ~12 INACTIVE, ~6 DRAFT, ~6 DISCONTINUED
- [ ] Products have realistic names (not "Product 1", "Product 2")
- [ ] SKUs follow format: `{CATEGORY}-{INDEX}-{RANDOM}`
- [ ] Prices vary by category (Electronics expensive, Books cheap)
- [ ] Restart doesn't duplicate (idempotent)

**Auth Service:**
- [ ] Returns 122 total users (120 customers + 2 system)
- [ ] Admin login works with `admin`/`admin123`
- [ ] Customer login works with any `{name}.{name}{index}` and `password123`
- [ ] All customers have CUSTOMER role
- [ ] Usernames/emails look realistic
- [ ] ~50% users have lastLoginAt
- [ ] ~5% users are disabled

**Inventory Service:**
- [ ] Returns exactly 120 inventory records
- [ ] Stock distribution: ~70% in stock, ~20% low stock, ~10% out of stock
- [ ] All product_id values exist in Product Service
- [ ] Reserved quantity is 0-5 and <= available quantity

**Order Service:**
- [ ] Returns exactly 150 orders
- [ ] Status distribution matches expectations (~40% Delivered, etc.)
- [ ] All orders have 1-5 items
- [ ] All order items reference valid products
- [ ] All customer IDs exist in Auth Service
- [ ] Cancelled orders have cancel reasons
- [ ] Total amounts are calculated correctly

**Cross-Service Integrity:**
- [ ] Inventory product_id → Product Service products exist
- [ ] Order customer_id → Auth Service users exist
- [ ] Order item product_id → Product Service products exist
- [ ] No orphan records

**Performance:**
- [ ] Each service seeds in <10 seconds
- [ ] Total startup time <2 minutes (including dependencies)
- [ ] No out-of-memory errors

**Idempotency:**
- [ ] Restart each service → no data duplication
- [ ] Counts remain stable

---

## Phase 8: Optional Enhancements

### 8.1: Gradle Task for Seeding

Add this to root `build.gradle.kts`:

```kotlin
// Seed all services data
tasks.register("seedAllData") {
    group = "application"
    description = "Seed data for all services (requires databases running)"

    dependsOn(
        ":product-service:bootRun",
        ":auth-service:bootRun",
        ":inventory-service:bootRun",
        ":order-service:bootRun"
    )

    doLast {
        println("All services seeded successfully!")
    }
}
```

**Usage:**
```bash
./gradlew seedAllData
```

### 8.2: Performance Testing Data

Create `LargeDataSeeder.java` for 10k+ records:

```java
@Component
@Profile("perf-test")
public class LargeDataSeeder implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // Generate 10,000 products for load testing
        List<ProductJpaEntity> products = productFactory.generateProducts(10000);
        productRepository.saveAll(products);
    }
}
```

**Usage:**
```bash
./gradlew bootRun --args='--spring.profiles.active=perf-test'
```

### 8.3: Demo Scenarios

Create specific demo data:

```java
@Component
@Profile("demo")
public class DemoScenarioSeeder implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // Scenario 1: Happy path order
        createHappyPathOrder();

        // Scenario 2: Failed payment
        createFailedPaymentOrder();

        // Scenario 3: Out of stock
        createOutOfStockScenario();
    }
}
```

---

## Common Issues & Solutions

### Issue: Migration Checksum Mismatch

**Error:**
```
FlywayException: Validate failed: Migration checksum mismatch for migration version 10
```

**Solution:**
```bash
# Clean database and re-run
docker-compose down -v
docker-compose up -d postgres
./gradlew bootRun
```

### Issue: Duplicate Key Violations

**Error:**
```
PSQLException: duplicate key value violates unique constraint
```

**Solution:**
- Ensure seeding code checks `count() > 0` before inserting
- Add `ON CONFLICT DO NOTHING` to SQL migrations
- Reset database if needed

### Issue: Connection Refused Between Services

**Error:**
```
RestClientException: Connection refused
```

**Solution:**
- Ensure dependent service is running
- Wait 15-30 seconds for service startup
- Check service URLs in `application-dev.yml`
- Verify port is correct (8081, 8082, 8083, 8086)

### Issue: DataFaker Not Found

**Error:**
```
Cannot resolve net.datafaker.Faker
```

**Solution:**
```bash
./gradlew --refresh-dependencies
./gradlew clean build
```

### Issue: Slow Seeding (>30 seconds)

**Causes:**
- Bcrypt password hashing (Auth Service)
- Large batch inserts without batch configuration
- SQL logging enabled

**Solution:**
```yaml
# application-dev.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
```

---

## Next Steps

After completing implementation:

1. **Test full flow:**
   ```bash
   ./scripts/seed-dev-environment.sh
   ```

2. **Verify all services:**
   - Check logs for success messages
   - Run API verification commands
   - Execute database integrity checks

3. **Share with team:**
   - Commit code to Git
   - Update team documentation
   - Demo seeding in team meeting

4. **Production considerations:**
   - Ensure `@Profile("dev")` is on all seeders
   - Verify production uses different profiles
   - Add monitoring for accidental seeding in prod

---

**Implementation Complete! 🎉**

You now have a fully functional data seeding system with:
- ✅ 120 realistic products
- ✅ 120 customer users
- ✅ 120 inventory records
- ✅ 150 orders with items
- ✅ Idempotent, reproducible, and production-safe

For questions or issues, refer to the troubleshooting section or Master Plan.
