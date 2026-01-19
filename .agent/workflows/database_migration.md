---
description: Database schema migration workflow with review and rollback strategy
---

# Database Migration Workflow

## ⚠️ Critical Rule
**ALL schema changes MUST be reviewed by Tech Lead or DBA before production deployment.**

## Steps

### 1. Planning
- **Analyze**: Determine impact on data and application uptime.
- **Plan**: Create migration plan in `docs/migrations/`.
  - Template: [.agent/templates/migration_plan_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/migration_plan_template.md)

### 2. Implementation
- **Script**: Write Flyway SQL script (`V{version}__{description}.sql`).
  - Template: [.agent/templates/migration_sql_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/migration_sql_template.md)
- **Rollback**: Provide corresponding rollback script.
- **Pattern**: Use **Expand-Contract** for breaking changes to ensure zero-downtime.

### 3. Verification
- **Local**: Test migration and rollback using Docker.
- **Staging**: Verify in staging environment with performance checks (EXPLAIN ANALYZE).

### 4. Human Review ⏸️
- Submit migration plan and SQL for approval (Tech Lead + DBA).

### 5. Production Deployment
- **Backup**: Always backup DB before execution.
- **Execute**: Run `flywayMigrate`. Use `CONCURRENTLY` for index creation.
- **Monitor**: Check for locks and performance degradation.

## Best Practices
- ✅ Use `V{version}__{description}.sql` naming.
- ✅ Always provide a rollback script.
- ✅ No `NOT NULL` without default on large tables.
- ✅ Add indexes on foreign keys.
- ✅ Split large migrations into smaller, safe tasks.

## Checklist
- [ ] Migration strategy designed
- [ ] SQL & Rollback scripts tested locally
- [ ] Migration plan approved by Tech Lead/DBA
- [ ] Tested successfully in staging
- [ ] Production backup verified
- [ ] Production verification successful
