---
description: Production incident response for P0/P1 bugs
---

# Hotfix Workflow

## ⚡ Critical Rule
**This workflow is for PRODUCTION INCIDENTS ONLY**
- P0: Complete service outage
- P1: Critical functionality broken, major customer impact

For regular bugs, use `/bugfix` workflow instead.

---

## Overview
This workflow ensures rapid response to production incidents while maintaining code quality and documentation.

---

## Incident Severity Classification

### P0 - Critical (Response: Immediate)
- **Complete service outage**
- **Data corruption**
- **Security breach**
- **Payment processing down**

**Response time**: < 15 minutes  
**Fix time target**: < 2 hours

### P1 - High (Response: < 1 hour)
- **Major feature broken**
- **Significant customer impact**
- **Performance degradation > 50%**
- **Third-party integration failure**

**Response time**: < 1 hour  
**Fix time target**: < 4 hours

---

## Steps

### 1. Incident Declaration
- **Create incident ticket** with severity label (P0/P1)
- **Start incident call** (for P0, mandatory)
- **Notify stakeholders**:
  - P0: Tech Lead, Product Owner, CTO
  - P1: Tech Lead, Product Owner
- **Assign incident commander** (Tech Lead or on-call engineer)

### 2. Immediate Assessment (5-15 minutes)
- **Identify affected service(s)**
- **Check recent deployments** (last 24 hours)
- **Review monitoring dashboards**:
  - Error rate spike?
  - Latency spike?
  - Infrastructure issues?
- **Check logs** for error patterns
- **Determine root cause** (best guess if time-critical)

### 3. Decide: Rollback or Hotfix?

#### Option A: Rollback (Preferred for P0)
**When to use**:
- Recent deployment caused the issue
- Root cause is clear
- Rollback is safe

**Steps**:
```bash
# Rollback to previous version
kubectl rollout undo deployment/{service-name} -n {environment}

# Or use CI/CD rollback
# Follow CI/CD rollback procedure from ci_cd.yaml
```

**Verify**:
- Service health restored
- Error rate back to normal
- No data corruption

**Then proceed to Step 7** (Post-Incident)

#### Option B: Hotfix (When rollback not possible)
**When to use**:
- Issue not caused by recent deployment
- Rollback would break other features
- Data migration required

**Proceed to Step 4**

### 4. Create Hotfix Branch
```bash
# Branch from main (production)
git checkout main
git pull origin main
git checkout -b hotfix/INC-XXX-{short-description}
```

**Example**:
```bash
git checkout -b hotfix/INC-789-fix-payment-timeout
```

### 5. Implement Fix (Fast but Safe)

#### Time-Critical Fix
- **Focus on minimal change** to restore service
- **Don't refactor** unrelated code
- **Don't add new features**
- **Add inline comments** explaining the fix

#### Testing
- **Write regression test** if time permits
- **Test locally** if possible
- **If P0 and no time**: Skip local testing, rely on staging

**Example**:
```java
// HOTFIX INC-789: Increase payment gateway timeout
// Root cause: Default 5s timeout too short for peak traffic
private static final Duration PAYMENT_TIMEOUT = Duration.ofSeconds(30); // was 5s
```

### 6. Expedited Deployment

#### Fast-Track PR
```bash
git add .
git commit -m "hotfix(payment-service): increase timeout to 30s

Refs: INC-789
Severity: P0"

git push origin hotfix/INC-789-fix-payment-timeout
```

**Create PR**:
- Title: `[HOTFIX P0] INC-789: Fix payment gateway timeout`
- Add label: `hotfix`, `P0` or `P1`
- Request review from **2 reviewers** (even for P0)

#### Accelerated Review
- **P0**: Review ASAP, < 30 minutes
- **P1**: Review within 2 hours
- **Focus on**: Does this fix the issue? Any obvious bugs?
- **Don't nitpick**: Code style can be fixed later

#### Deploy
```bash
# Merge to main
git checkout main
git merge --no-ff hotfix/INC-789-fix-payment-timeout
git tag -a v1.0.1 -m "Hotfix: Payment gateway timeout"
git push origin main --tags

# Merge back to develop
git checkout develop
git merge --no-ff hotfix/INC-789-fix-payment-timeout
git push origin develop

# Delete hotfix branch
git branch -d hotfix/INC-789-fix-payment-timeout
git push origin --delete hotfix/INC-789-fix-payment-timeout
```

**Trigger deployment**:
- P0: Deploy directly to production (with approval)
- P1: Deploy to staging first, then production

**Monitor deployment**:
- Watch error rate
- Watch latency
- Watch logs for new errors
- Keep incident call open during deployment

### 7. Verify Fix
- **Service health restored?**
- **Error rate back to baseline?**
- **No new errors introduced?**
- **Business metrics recovering?**

**Verification checklist**:
- [ ] Service up and healthy
- [ ] Error rate < 1%
- [ ] Latency back to normal
- [ ] All health checks passing
- [ ] Customer reports confirm fix

### 8. Post-Incident (Within 24 hours)

#### Immediate (< 1 hour after fix)
- **Update incident ticket** with resolution
- **Notify stakeholders** that incident is resolved
- **Internal announcement**: "Issue resolved, monitoring continues"

#### Post-Incident Review (< 24 hours)
Create post-mortem document:

**Template**:
```markdown
# Post-Mortem: INC-789 Payment Gateway Timeout

## Incident Summary
- **Severity**: P0
- **Start time**: 2026-01-19 14:00 UTC
- **Detection time**: 2026-01-19 14:05 UTC
- **Resolution time**: 2026-01-19 15:30 UTC
- **Duration**: 1h 30m
- **Impact**: 500 failed payments, ~$50k revenue at risk

## Timeline
- 14:00 - Traffic spike begins
- 14:05 - Alerts fired: High error rate
- 14:10 - Incident declared, incident call started
- 14:20 - Root cause identified: Payment timeout
- 14:30 - Hotfix merged
- 14:45 - Deployed to production
- 15:00 - Error rate dropping
- 15:30 - Incident resolved

## Root Cause
Payment gateway timeout was 5s, insufficient during peak traffic.
No load testing was done with peak traffic simulation.

## Resolution
Increased timeout from 5s to 30s based on payment gateway SLA.

## Action Items
- [ ] Add load testing to CI/CD (Owner: @engineer, Due: 2026-01-26)
- [ ] Review all external API timeouts (Owner: @tech-lead, Due: 2026-01-23)
- [ ] Add alert for payment gateway latency (Owner: @sre, Due: 2026-01-20)
- [ ] Improve monitoring for payment flow (Owner: @engineer, Due: 2026-01-26)

## Lessons Learned
- Load testing should include peak traffic scenarios
- External API timeouts should be based on SLA, not guesses
- Early detection is key - better monitoring needed
```

#### Follow-up Actions
- **Schedule post-mortem meeting** (blameless)
- **Create tickets for action items**
- **Track action items to completion**
- **Update runbooks** if needed

---

## Hotfix Checklist

### Incident Response
- [ ] Incident ticket created with severity
- [ ] Incident call started (P0 only)
- [ ] Stakeholders notified
- [ ] Incident commander assigned
- [ ] Root cause identified

### Fix Implementation
- [ ] Decision made: Rollback vs Hotfix
- [ ] Hotfix branch created from main
- [ ] Minimal fix implemented
- [ ] Regression test added (if time permits)
- [ ] Code reviewed by 2 reviewers

### Deployment
- [ ] PR merged to main
- [ ] Version tagged
- [ ] Merged back to develop
- [ ] Deployed to production
- [ ] Deployment monitored

### Verification
- [ ] Service health restored
- [ ] Error rate back to baseline
- [ ] No new errors introduced
- [ ] Customer reports verified

### Post-Incident
- [ ] Incident ticket updated
- [ ] Stakeholders notified
- [ ] Post-mortem document created
- [ ] Post-mortem meeting scheduled
- [ ] Action items created and tracked

---

## Critical Reminders

❌ **Don't**:
- Don't panic - stay calm and methodical
- Don't skip review even for P0
- Don't refactor during hotfix
- Don't add new features
- Don't deploy without monitoring
- Don't forget to merge back to develop

✅ **Do**:
- Communicate constantly
- Document everything
- Monitor after deployment
- Learn from incidents
- Follow up on action items
- Improve monitoring and alerting

---

## Escalation Path

**If fix doesn't work**:
1. Rollback the hotfix
2. Escalate to senior engineer
3. Consider alternative solutions
4. Keep stakeholders updated

**If you need help**:
1. Call for backup engineer
2. Escalate to Tech Lead
3. Escalate to CTO (for P0)

---

## Emergency Contacts

- **On-call Engineer**: [Pager number]
- **Tech Lead**: [Phone/Slack]
- **SRE Team**: [Slack channel]
- **CTO**: [Phone] (P0 only)

---

**Remember**: The goal is to restore service as quickly as possible while maintaining minimum quality standards. Speed is important, but don't skip critical safety checks.
