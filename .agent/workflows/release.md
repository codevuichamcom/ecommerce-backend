---
description: Production release workflow
---

# Release Workflow

## Overview
This workflow guides the process of releasing a new version to production.

---

## Release Types

### Major Release (X.0.0)
- **Breaking API changes**
- **Major architecture changes**
- **Requires migration guide**

**Example**: 1.0.0 → 2.0.0

### Minor Release (1.X.0)
- **New features**
- **Backward compatible**
- **New API endpoints**

**Example**: 1.0.0 → 1.1.0

### Patch Release (1.0.X)
- **Bug fixes only**
- **Security patches**
- **No new features**

**Example**: 1.0.0 → 1.0.1

---

## Steps

### 1. Preparation (1-2 weeks before release)

#### Create Release Planning Document

**Template**: `docs/releases/release-1.1.0-plan.md`

```markdown
# Release 1.1.0 Plan

## Release Date
Target: 2026-01-26 (Saturday)

## Release Manager
@tech-lead

## Release Type
Minor - New features, backward compatible

## Features Included
- [ ] ECOM-123: Add product description
- [ ] ECOM-456: Implement payment retry logic
- [ ] ECOM-789: Add email notifications

## Bug Fixes
- [ ] ECOM-234: Fix inventory race condition
- [ ] ECOM-567: Fix payment timeout

## Database Migrations
- [ ] V2: Add product description column
- [ ] V3: Add payment_attempts table

## Breaking Changes
None

## Deployment Plan
1. Deploy to dev - 2026-01-20
2. QA testing in staging - 2026-01-21 to 2026-01-25
3. Production deployment - 2026-01-26 10:00 UTC

## Rollback Plan
- Revert to v1.0.0 if critical issues
- Database rollback scripts prepared

## Communication Plan
- Internal announcement: 2026-01-24
- Customer announcement: 2026-01-26 (if customer-facing changes)
```

#### Verify All Features Complete
- [ ] All feature PRs merged to `develop`
- [ ] All tests passing
- [ ] Code coverage meets threshold
- [ ] Documentation updated

### 2. Create Release Branch

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/1.1.0

# Push release branch
git push origin release/1.1.0
```

### 3. Prepare Release

#### Update Version Numbers

**In `build.gradle.kts`** (or root build config):
```kotlin
version = "1.1.0"
```

**In Docker Compose**:
```yaml
image: ecommerce/product-service:1.1.0
```

#### Update Changelog

**`CHANGELOG.md`**:
```markdown
# Changelog

## [1.1.0] - 2026-01-26

### Added
- Product description field (ECOM-123)
- Payment retry logic (ECOM-456)
- Email notifications for orders (ECOM-789)

### Fixed
- Inventory race condition (ECOM-234)
- Payment gateway timeout (ECOM-567)

### Changed
- Increased payment timeout from 5s to 30s

### Deprecated
- None

### Removed
- None

### Security
- Updated Spring Boot to 3.2.2 (CVE-2024-XXXXX)

## [1.0.0] - 2026-01-12
...
```

#### Commit Changes

```bash
git add build.gradle.kts CHANGELOG.md docker-compose.yml
git commit -m "chore: prepare release 1.1.0"
git push origin release/1.1.0
```

### 4. Deploy to Staging

```bash
# Merge release branch to staging (if separate staging branch)
# Or deploy directly from release/1.1.0

# Run database migrations
./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://staging-db:5432/ecommerce_db

# Deploy services
docker-compose -f docker-compose.staging.yml up -d
```

### 5. QA Testing in Staging

**QA Checklist**:

#### Functional Testing
- [ ] All new features work as expected
- [ ] Existing features still work (regression)
- [ ] Bug fixes verified
- [ ] Edge cases tested

#### Integration Testing
- [ ] Service-to-service communication working
- [ ] Kafka events flowing correctly
- [ ] Database queries performing well
- [ ] External API integrations working

#### Performance Testing
```bash
# Run load test
k6 run loadtest.js

# Check metrics
- Response time p95 < 500ms
- Error rate < 0.1%
- Throughput meets SLA
```

#### Security Testing
- [ ] Dependency scan: No critical vulnerabilities
- [ ] OWASP checks passing
- [ ] Authentication/authorization working
- [ ] No sensitive data exposed

#### Database Migration Testing
- [ ] Migrations run successfully
- [ ] Data integrity maintained
- [ ] Rollback tested and working
- [ ] Performance impact acceptable

### 6. Create Release PR

**PR**: `release/1.1.0` → `main`

**Title**: `[Release] v1.1.0`

**Description**:
```markdown
## Release v1.1.0

### Release Date
2026-01-26

### Release Type
Minor - Backward compatible feature release

### Features
- Product description field
- Payment retry logic
- Email notifications

### Bug Fixes
- Inventory race condition
- Payment gateway timeout

### Database Migrations
- V2: products.description
- V3: payment_attempts table

### Testing
- [ ] All tests passing
- [ ] QA regression complete
- [ ] Performance tested
- [ ] Security scan clean
- [ ] Migration tested in staging

### Approvals Required
- [ ] Tech Lead
- [ ] Product Owner
- [ ] QA Lead

### Deployment Plan
See: docs/releases/release-1.1.0-plan.md

### Rollback Plan
Revert to v1.0.0, run rollback migrations
```

**Request approvals** from:
- Tech Lead
- Product Owner
- QA Lead

### 7. Production Deployment

#### Pre-deployment Checklist
- [ ] All approvals received
- [ ] Staging testing complete
- [ ] Rollback plan ready
- [ ] Database backup created
- [ ] Team notified
- [ ] Monitoring dashboards ready

#### Deployment Steps

```bash
# 1. Merge release to main
git checkout main
git pull origin main
git merge --no-ff release/1.1.0
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin main --tags

# 2. Merge release back to develop
git checkout develop
git merge --no-ff release/1.1.0
git push origin develop

# 3. Delete release branch
git branch -d release/1.1.0
git push origin --delete release/1.1.0

# 4. Deploy to production (via CI/CD)
# This triggers on tag push
# Or manual deployment:
./gradlew flywayMigrate -Dflyway.url=jdbc:postgresql://prod-db:5432/ecommerce_db
kubectl apply -f k8s/production/

# OR use Canary deployment
# See ci_cd.yaml for canary strategy
```

#### Monitoring During Deployment

**Watch key metrics**:
- Error rate
- Response time (p95, p99)
- Request throughput
- Database connection pool
- Kafka consumer lag
- JVM memory
- CPU usage

```bash
# Watch deployment
kubectl rollout status deployment/product-service -n production

# Check logs
kubectl logs -f deployment/product-service -n production

# Check metrics
# Open Grafana dashboard
```

**Canary deployment checkpoints**:
- 10% traffic: Monitor for 30 minutes
- 50% traffic: Monitor for 1 hour
- 100% traffic: Continue monitoring

### 8. Post-Deployment Verification

#### Smoke Tests
```bash
# Health checks
curl https://api.ecommerce.com/actuator/health

# Key endpoints
curl https://api.ecommerce.com/api/v1/products
curl https://api.ecommerce.com/api/v1/orders

# Database
SELECT version, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
```

#### Verification Checklist
- [ ] All services healthy
- [ ] Error rate normal (<0.1%)
- [ ] Response time normal (<500ms p95)
- [ ] No errors in logs
- [ ] Database migrations successful
- [ ] Kafka consumers consuming
- [ ] Key user flows working

### 9. Announcement

**Internal announcement** (Slack):
```
🚀 Release v1.1.0 deployed to production

New features:
- Product descriptions now available
- Improved payment retry logic
- Email notifications for orders

Bug fixes:
- Fixed inventory race condition
- Fixed payment timeout issues

Docs: https://docs.ecommerce.com/releases/v1.1.0
```

**Customer announcement** (if applicable):
```
We're excited to announce new features:
- Detailed product descriptions
- More reliable payment processing
- Order email notifications

Read more: https://blog.ecommerce.com/release-1.1.0
```

### 10. Post-Release

#### Create Release Notes

**GitHub Release**:
- Tag: v1.1.0
- Title: Release v1.1.0 - Product Descriptions & Payment Improvements
- Description: Copy from CHANGELOG.md
- Attach artifacts if needed

#### Update Documentation
- [ ] API documentation updated
- [ ] User guide updated (if customer-facing)
- [ ] Runbooks updated
- [ ] Architecture diagrams updated

#### Retrospective (within 1 week)

**Template**: `docs/releases/release-1.1.0-retrospective.md`

```markdown
# Release 1.1.0 Retrospective

## What Went Well
- Smooth deployment with canary
- No rollbacks needed
- QA caught critical issue in staging

## What Didn't Go Well
- Migration took longer than expected
- Staging environment had data sync issues

## Action Items
- [ ] Improve staging data sync process (Owner: @engineer)
- [ ] Add migration performance tests (Owner: @dba)
- [ ] Update deployment runbook (Owner: @sre)

## Metrics
- Deployment duration: 2 hours
- Downtime: 0 minutes
- Rollbacks: 0
- Critical bugs: 0
- Minor bugs: 1 (fixed in 1.1.1)
```

---

## Rollback Procedure

**If critical issue discovered**:

### 1. Decide: Hotfix or Rollback?

**Rollback if**:
- Service completely broken
- Data corruption occurring
- Security vulnerability

**Hotfix if**:
- Minor bug, not critical
- Rollback would cause more issues
- Can be fixed quickly

### 2. Execute Rollback

```bash
# Revert to previous version
kubectl rollout undo deployment/product-service -n production

# Or deploy previous tag
kubectl set image deployment/product-service product-service=ecommerce/product-service:1.0.0 -n production

# Rollback database migrations
psql -U postgres -d ecommerce_db -f V3_rollback.sql
psql -U postgres -d ecommerce_db -f V2_rollback.sql

# Update Flyway history if needed
DELETE FROM flyway_schema_history WHERE version IN ('2', '3');
```

### 3. Verify Rollback

- [ ] Services healthy
- [ ] Error rate back to normal
- [ ] No data loss
- [ ] Customer impact minimized

### 4. Post-Rollback

- Create incident report
- Investigate root cause
- Fix issue in develop
- Plan re-release

---

## Release Calendar

**Recommended schedule**:
- **Major releases**: Quarterly (Q1, Q2, Q3, Q4)
- **Minor releases**: Monthly
- **Patch releases**: As needed (within 1 week of bug discovery)

**Release windows**:
- **Preferred**: Saturday 10:00-14:00 UTC (low traffic)
- **Avoid**: Friday, end of month, before major holidays

---

## Checklist

- [ ] Release plan created
- [ ] All features complete
- [ ] Release branch created
- [ ] Version numbers updated
- [ ] Changelog updated
- [ ] Deployed to staging
- [ ] QA testing complete
- [ ] Performance tested
- [ ] Security scan passed
- [ ] Release PR approved
- [ ] Database backup created
- [ ] Production deployment complete
- [ ] Post-deployment verification passed
- [ ] Announcement sent
- [ ] Release notes published
- [ ] Documentation updated

---

## Best Practices

✅ **Do**:
- Release on low-traffic days
- Have rollback plan ready
- Monitor during deployment
- Communicate with stakeholders
- Document everything
- Celebrate successes!

❌ **Don't**:
- Release on Friday afternoon
- Skip QA testing
- Deploy without approval
- Ignore monitoring alerts
- Rush the release
