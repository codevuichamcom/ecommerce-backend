# Runbook: {Alert Name}

## Metadata
- **Alert Name**: {alert_name}
- **Severity**: {Critical/Warning}
- **Source**: {Prometheus/Alertmanager/etc}
- **Team**: {Team Name}

## Impact
- {What is the customer impact?}
- {Which services are affected?}

## Diagnosis Steps
1. **Check Dashboard**: {Link to Grafana Dashboard}
2. **Check Logs**: {Link to Log Aggregator}
3. **Check Distributed Tracing**: {Link to Jaeger/Zipkin}
4. **Identify Affected Service**: {Queries or steps}

## Resolution Steps
1. **Option 1: Rollback**
   - {Command: kubectl rollout undo...}
2. **Option 2: Scaling**
   - {Command: kubectl scale...}
3. **Option 3: Manual Intervention**
   - {Step-by-step instructions}

## Escalation Path
1. **Primary On-Call**: @{username}
2. **Secondary**: Tech Lead / Tech Lead / SRE
3. **Management**: Product Owner / CTO
