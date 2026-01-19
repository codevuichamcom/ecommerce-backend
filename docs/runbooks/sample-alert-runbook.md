# Runbook: HighErrorRate

## Alert Information
- **Alert Name**: HighErrorRate
- **Severity**: Critical 🔴
- **Source**: Prometheus / Alertmanager
- **Team**: Backend Engineering

## Impact
- Users may experience failed requests (5xx errors) on specific endpoints.
- Integration points or downstream services might be failing.
- Revenue loss if critical flows (checkout, payment) are affected.

## Diagnosis Steps
1. **Check Dashboard**: Navigate to the "Service Overview" Grafana dashboard.
   - Observe the "Error Rate" panel per service.
   - Identify which service/endpoint is producing the errors.
2. **Check Logs**:
   - Query logs in Elasticsearch/CloudWatch for the affected service.
   - Filter by `level: "ERROR"`.
   - Look for common `exception_class` or error messages.
   - Use `correlation_id` to trace a single failed request.
3. **Check Distributed Tracing**:
   - Open Jaeger/Zipkin.
   - Search for traces with `error=true`.
   - Identify if the error originates in the service itself or a downstream dependency (DB, Kafka, External API).
4. **Check Health**:
   - Verify `/actuator/health` of the affected service.
   - Check if downstream dependencies are "UP".

## Resolution Steps
### Option A: Transient Infrastructure Issue
- If it's a transient DB connection issue or Kafka lag, wait for auto-recovery or restart the affected pod/instance.
  ```bash
  kubectl rollout restart deployment/<service-name>
  ```

### Option B: Recent Deployment
- If the errors started immediately after a deployment, **Rollback** is preferred.
  ```bash
  kubectl rollout undo deployment/<service-name>
  ```

### Option C: Bug in Code
- If a specific bug is identified, follow the [Hotfix Workflow](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/hotfix.md).

## Escalation Path
1. **Primary On-Call**: Backend Engineer
2. **Secondary**: Tech Lead / SRE
3. **Management**: Product Manager (if impact is high)

---
**Last Updated**: 2026-01-19
**Version**: 1.0
