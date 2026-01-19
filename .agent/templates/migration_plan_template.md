# Migration Plan: {Title}

## Metadata
- **Ticket**: {Ticket-ID}
- **Author**: @{username}
- **Date**: {YYYY-MM-DD}
- **Services affected**: {service_name}
- **Risk level**: {Low/Medium/High}
- **Estimated downtime**: {Zero/Maintenance Window}

## Changes
- {point_1}
- {point_2}

## SQL Scripts
- `V{version}__description.sql` - Forward migration
- `V{version}_rollback.sql` - Rollback script

## Backward Compatibility
{✅ Yes / ❌ No - Reason}

## Performance Impact
- {operation}: {estimated_time} ({row_count} rows)
- **Total**: ~{total_time}

## Rollback Strategy
```sql
{rollback_sql_commands}
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
-- SQL commands to verify the change
```

## Risks
- **Risk**: {Risk Description}
- **Mitigation**: {How to mitigate}

## Approvals
- [ ] Tech Lead: @tech-lead
- [ ] DBA: @dba (for production)
