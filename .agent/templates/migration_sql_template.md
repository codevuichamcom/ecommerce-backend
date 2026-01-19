-- {V_version}__{description}.sql
-- Purpose: {Describe what this migration does}
-- Author: @{username}
-- Date: {YYYY-MM-DD}
-- Ticket: {Ticket-ID}
-- Risk: {Low/Medium/High}
-- Rollback: {V_version}_rollback.sql

-- Migration
{sql_migration_commands}

-- Add index if needed (Use CONCURRENTLY in production)
-- CREATE INDEX CONCURRENTLY {index_name} ON {table_name}({column_name});

-- Update existing rows with default if needed
-- UPDATE {table_name} SET {column_name} = {default_value} WHERE {column_name} IS NULL;

-- Add comment
COMMENT ON COLUMN {table_name}.{column_name} IS '{description}';

---

-- Rollback script (V{version}_rollback.sql)
-- Purpose: Rollback for {V_version}__{description}.sql

-- DROP INDEX IF EXISTS {index_name};
-- ALTER TABLE {table_name} DROP COLUMN IF EXISTS {column_name};
