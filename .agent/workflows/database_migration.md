---
description: Database schema migration workflow with review and rollback strategy
---

# Database Migration Workflow

## ⚠️ Critical Rule
**ALL schema changes MUST be reviewed by Tech Lead or DBA before production deployment**

Reason: Schema changes can break production, cause data loss, or create performance issues.

---

## Overview
This workflow ensures safe database schema changes with proper review, testing, rollback strategy, and zero-downtime deployment.

---

## Migration Types

### 1. Additive Changes (Low Risk)
- Add new table
- Add new column (nullable or with default)
- Add new index
- Add new constraint

**Risk**: Low  
**Rollback**: Easy (just drop the new objects)

### 2. Modification Changes (Medium Risk)
- Rename column
- Change column type
- Modify constraint
- Drop nullable constraint

**Risk**: Medium  
**Rollback**: Requires careful planning

### 3. Destructive Changes (High Risk)
- Drop table
- Drop column
- Change column to NOT NULL
- Add foreign key to large table

**Risk**: High  
**Rollback**: Difficult or impossible  
**Requires**: Extra careful review and backups

---

## Steps

### 1. Analyze Change Requirement
- **What data needs to change?**
- **Why is this change needed?**
- **What's the impact on existing data?**
- **What's the impact on running application?**
- **Can this be done without downtime?**

### 2. Design Migration Strategy

#### For Additive Changes
```sql
-- Simple: Just add the new column
ALTER TABLE products 
ADD COLUMN description TEXT;
```

#### For Breaking Changes - Use Expand-Contract Pattern
**Phase 1: Expand** (Add new structure alongside old)
```sql
-- V1.1__add_new_email_column.sql
ALTER TABLE users 
ADD COLUMN email_address VARCHAR(255);

-- Keep old 'email' column for now
```

**Phase 2: Dual-write** (Application writes to both old and new)
```java
// Application code writes to both columns
user.setEmail(email);
user.setEmailAddress(email);
```

**Phase 3: Migrate** (Copy data from old to new)
```sql
-- V1.2__migrate_email_data.sql
UPDATE users 
SET email_address = email 
WHERE email_address IS NULL;
```

**Phase 4: Switch** (Application reads from new)
```java
// Application now only uses emailAddress
String email = user.getEmailAddress();
```

**Phase 5: Contract** (Drop old structure)
```sql
-- V1.3__drop_old_email_column.sql
ALTER TABLE users 
DROP COLUMN email;
```

### 3. Write Flyway Migration

#### Naming Convention
```
V{version}__{description}.sql
```

**Examples**:
```
V1__init_schema.sql
V2__add_product_description.sql
V3__create_payment_table.sql
V4.1__add_email_address_column.sql
V4.2__migrate_email_data.sql
V4.3__drop_old_email_column.sql
```

#### Migration Template
```sql
-- V2__add_product_description.sql
-- Purpose: Add description field to products
-- Author: @engineer
-- Date: 2026-01-19
-- Ticket: ECOM-123
-- Risk: Low (additive change)
-- Rollback: V2_rollback.sql

-- Migration
ALTER TABLE products 
ADD COLUMN description TEXT;

-- Add index if needed
CREATE INDEX idx_products_description ON products(description);

-- Update existing rows with default if needed
UPDATE products 
SET description = 'No description available' 
WHERE description IS NULL;

-- Add comment
COMMENT ON COLUMN products.description IS 'Product description';
```

#### Rollback Script (Required for all migrations)
```sql
-- V2_rollback.sql
-- Rollback for V2__add_product_description.sql

DROP INDEX IF EXISTS idx_products_description;
ALTER TABLE products DROP COLUMN IF EXISTS description;
```

### 4. Test Migration Locally

```bash
# Start local database
docker-compose up -d postgres

# Run migration
./gradlew flywayMigrate

# Verify schema
./gradlew flywayInfo

# Test rollback
./gradlew flywayUndo  # if flyway undo is configured

# Or manually run rollback script
psql -U postgres -d ecommerce_db -f src/main/resources/db/migration/V2_rollback.sql

# Re-run migration
./gradlew flywayMigrate
```

**Checks**:
- [ ] Migration runs successfully
- [ ] Schema matches expected state
- [ ] Existing data not corrupted
- [ ] Application still works
- [ ] Rollback script works
- [ ] Can re-run migration after rollback

### 5. Create Migration Plan Document

**Template**: `docs/migrations/ECOM-123-migration-plan.md`

```markdown
# Migration Plan: Add Product Description

## Metadata
- **Ticket**: ECOM-123
- **Author**: @engineer
- **Date**: 2026-01-19
- **Services affected**: product-service
- **Risk level**: Low
- **Estimated downtime**: Zero

## Changes
- Add `description` column to `products` table
- Add index on `description` column
- Update existing rows with default value

## SQL Scripts
- `V2__add_product_description.sql` - Forward migration
- `V2_rollback.sql` - Rollback script

## Backward Compatibility
✅ Yes - New column is nullable, existing queries unaffected

## Performance Impact
- Index creation: ~5 seconds (10k rows)
- Data update: ~2 seconds
- **Total**: ~7 seconds

## Rollback Strategy
```sql
DROP INDEX idx_products_description;
ALTER TABLE products DROP COLUMN description;
```

## Testing Plan
- [ ] Local testing completed
- [ ] Integration tests pass
- [ ] Performance test in staging
- [ ] Rollback tested in staging
- [ ] Manual verification in staging

## Deployment Plan
1. Deploy to dev - verify
2. Deploy to staging - verify + performance test
3. Deploy to production during maintenance window

## Verification Steps
```sql
-- Verify column exists
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'products' AND column_name = 'description';

-- Verify index exists
SELECT indexname FROM pg_indexes 
WHERE tablename = 'products' AND indexname = 'idx_products_description';

-- Verify data
SELECT COUNT(*) FROM products WHERE description IS NOT NULL;
```

## Risks
- **Risk**: Index creation might lock table
- **Mitigation**: Use `CONCURRENTLY` for index creation in production

## Approvals
- [ ] Tech Lead: @tech-lead
- [ ] DBA: @dba (for production)
```

### 6. Human Review ⏸️
**STOP HERE - Wait for approval**

**Reviewers**:
- **Dev/Staging**: Tech Lead
- **Production**: Tech Lead + DBA

**Review checklist**:
- [ ] Migration follows naming convention
- [ ] Rollback script provided
- [ ] Backward compatibility considered
- [ ] Performance impact assessed
- [ ] Testing plan adequate
- [ ] Risk level appropriate
- [ ] Migration plan document complete

**Common issues to check**:
- Adding NOT NULL without default on large table
- Missing index on foreign key
- Renaming column breaks existing queries
- Data type change causes data loss
- Constraint breaks existing data

### 7. Test in Staging

```bash
# Deploy to staging
./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://staging-db:5432/ecommerce_db

# Verify
./gradlew flywayInfo

# Run application tests
./gradlew test integrationTest

# Performance test
# Monitor query performance
# Check for slow queries
```

**Performance checks**:
```sql
-- Check table size
SELECT pg_size_pretty(pg_total_relation_size('products'));

-- Check query performance
EXPLAIN ANALYZE 
SELECT * FROM products WHERE description LIKE '%laptop%';
```

### 8. Deploy to Production

#### Pre-deployment
- [ ] Backup database
- [ ] Notify team of deployment
- [ ] Schedule during low-traffic window (if needed)
- [ ] Have rollback script ready

#### Deployment
```bash
# For zero-downtime: Use CONCURRENTLY for indexes
-- V2__add_product_description.sql
ALTER TABLE products ADD COLUMN description TEXT;
CREATE INDEX CONCURRENTLY idx_products_description ON products(description);

# Run migration
./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://prod-db:5432/ecommerce_db
```

#### Monitoring during deployment
```bash
# Monitor migration progress
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

# Monitor locks
SELECT pid, usename, query, state 
FROM pg_stat_activity 
WHERE datname = 'ecommerce_db';

# Monitor performance
SELECT schemaname, tablename, 
       seq_scan, seq_tup_read, 
       idx_scan, idx_tup_fetch
FROM pg_stat_user_tables 
WHERE tablename = 'products';
```

### 9. Verify in Production

**Verification checklist**:
- [ ] Migration status: Success
- [ ] Schema matches expected
- [ ] Application health checks passing
- [ ] No errors in application logs
- [ ] Key queries still performant
- [ ] Business metrics stable

```sql
-- Verify migration
SELECT * FROM flyway_schema_history 
WHERE version = '2' AND success = true;

-- Verify schema
\d products

-- Verify data integrity
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM products WHERE description IS NOT NULL;
```

### 10. Post-Deployment

- **Update migration status** in ticket
- **Document any issues** encountered
- **Update runbooks** if needed
- **Archive migration plan** in `docs/migrations/`

---

## Zero-Downtime Migration Techniques

### 1. Add Column (Safe)
```sql
-- Add nullable column
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### 2. Add Index (Use CONCURRENTLY)
```sql
-- Safe: Doesn't block writes
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
```

### 3. Drop Column (Expand-Contract)
**Phase 1**: Stop writing to column (deploy app)  
**Phase 2**: Drop column (run migration)

### 4. Rename Column (Expand-Contract)
**Phase 1**: Add new column  
**Phase 2**: Dual-write to both  
**Phase 3**: Migrate data  
**Phase 4**: Switch reads to new column  
**Phase 5**: Drop old column

### 5. Add NOT NULL Constraint (Multi-step)
```sql
-- Step 1: Add column as nullable
ALTER TABLE users ADD COLUMN email VARCHAR(255);

-- Step 2: Populate data
UPDATE users SET email = ... WHERE email IS NULL;

-- Step 3: Add NOT NULL constraint
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
```

---

## Rollback Procedures

### Automatic Rollback (Flyway Undo)
```bash
# Configure in build.gradle.kts
flyway {
    undoSqlMigrationPrefix = "U"
}

# Create undo script: U2__undo_add_description.sql
# Run undo
./gradlew flywayUndo
```

### Manual Rollback
```bash
# Run rollback script
psql -U postgres -d ecommerce_db -f V2_rollback.sql

# Or use Flyway repair
./gradlew flywayRepair

# Update Flyway history if needed
DELETE FROM flyway_schema_history WHERE version = '2';
```

### When Rollback Not Possible
- **Data already migrated**: Keep old and new columns
- **Constraints added**: May need to relax constraints
- **Data deleted**: Restore from backup

---

## Best Practices

✅ **Do**:
- Always test in dev and staging first
- Always provide rollback script
- Use `CONCURRENTLY` for index creation
- Add comments to schema changes
- Split large migrations into smaller steps
- Monitor performance during migration
- Backup before production migration
- Use expand-contract for breaking changes

❌ **Don't**:
- Modify `flyway_schema_history` manually (unless rollback)
- Deploy schema changes on Friday afternoon
- Skip backward compatibility check
- Run untested migrations in production
- Add foreign keys without indexes
- Change data type without checking data
- Drop columns without checking dependencies

---

## Example Checklist

- [ ] Change requirement analyzed
- [ ] Migration strategy designed
- [ ] Flyway migration written
- [ ] Rollback script written
- [ ] Migration plan document created
- [ ] Tested locally
- [ ] Human review completed
- [ ] Tested in staging
- [ ] Performance verified in staging
- [ ] Production backup created
- [ ] Deployed to production
- [ ] Verified in production
- [ ] Post-deployment complete

---

## Emergency Rollback

**If migration fails in production**:

1. **Stop the migration** (if in progress)
2. **Run rollback script immediately**
3. **Verify rollback success**
4. **Redeploy previous application version** (if needed)
5. **Create incident ticket**
6. **Investigate root cause**
7. **Fix and re-test before retry**

```bash
# Emergency rollback
psql -U postgres -d ecommerce_db -f V2_rollback.sql

# Verify
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

# If needed, manually fix Flyway history
DELETE FROM flyway_schema_history WHERE version = '2' AND success = false;
```

---

**Remember**: Database migrations are one-way operations in production. Always err on the side of caution and get proper review before deployment.
