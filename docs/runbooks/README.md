# 📋 Operational Runbooks

Step-by-step procedures for responding to common incidents and alerts.

---

## What is a Runbook?

A **runbook** is a detailed procedure for diagnosing and resolving a specific operational issue. Each runbook provides:
- Alert details and symptoms
- Diagnosis steps
- Resolution procedures
- Escalation path

---

## Runbook Index

| Runbook | Severity | MTTR Target | Last Updated |
|---------|----------|-------------|--------------|
| [Kafka Consumer Lag](kafka-consumer-lag.md) | Warning | 15 min | 2026-01-19 |
| [Database Connection Pool Exhausted](database-connection-pool.md) | Critical | 5 min | 2026-01-19 |
| [High Memory Usage](high-memory-usage.md) | Warning | 30 min | 2026-01-19 |
| [Order Stuck in Pending](order-stuck-pending.md) | Critical | 10 min | 2026-01-19 |
| [Service Restart](service-restart.md) | Info | 5 min | 2026-01-19 |
| [Disaster Recovery](disaster-recovery.md) | Critical | 60 min | 2026-01-19 |

---

## Using Runbooks

### When to Use

- **During incidents**: Follow step-by-step to resolve issues
- **During on-call**: Quick reference for common problems
- **For training**: Learn how to handle incidents

### How to Use

1. **Identify the issue**: Match symptoms to runbook
2. **Follow diagnosis steps**: Gather information
3. **Execute resolution**: Apply fixes carefully
4. **Document**: Note what worked
5. **Post-mortem**: Update runbook if needed

---

## Severity Levels

| Severity | Response Time | Example |
|----------|---------------|---------|
| **Critical** | Immediate | Service down, data loss |
| **Warning** | 15 minutes | High latency, resource usage |
| **Info** | Next business day | Routine maintenance |

---

## Escalation Path

1. **L1 Support**: Follow runbook, basic troubleshooting
2. **L2 Support**: Advanced troubleshooting, code analysis
3. **On-Call Engineer**: PagerDuty alert
4. **Tech Lead**: Critical incidents, architectural decisions
5. **CTO**: Major outages, security incidents

---

## Creating a New Runbook

1. Copy the [sample template](../runbooks/sample-alert-runbook.md)
2. Fill in all sections
3. Test the procedures
4. Submit for review
5. Update this index

---

## Runbook Maintenance

- **Review**: Quarterly
- **Update**: After each incident
- **Test**: Annually (chaos engineering)

---

**Last Updated**: 2026-01-19  
**Maintained By**: SRE Team
