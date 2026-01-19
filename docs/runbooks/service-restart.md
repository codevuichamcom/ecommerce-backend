# Runbook: Service Restart

**Severity**: Info  
**MTTR Target**: 5 minutes

---

## When to Restart

- Configuration changes
- Memory leaks (temporary fix)
- Unresponsive service
- Deployment rollback

---

## Pre-Restart Checklist

- [ ] Check if service is critical path
- [ ] Verify other instances healthy
- [ ] Notify team in Slack
- [ ] Check current load

---

## Restart Procedures

### Kubernetes

```bash
# Graceful restart (rolling)
kubectl rollout restart deployment/product-service

# Check status
kubectl rollout status deployment/product-service

# Verify pods
kubectl get pods -l app=product-service
```

### Docker Compose

```bash
# Restart single service
docker-compose restart product-service

# Check logs
docker-compose logs -f product-service
```

### Systemd

```bash
# Restart service
sudo systemctl restart product-service

# Check status
sudo systemctl status product-service
```

---

## Post-Restart Verification

### 1. Health Check

```bash
curl http://localhost:8081/actuator/health
```

**Expected**:
```json
{"status":"UP"}
```

### 2. Check Logs

```bash
# Look for startup errors
kubectl logs product-service-xxx | grep ERROR
```

### 3. Monitor Metrics

```bash
# Check request rate returns to normal
# Grafana dashboard or Prometheus
```

---

## Rollback Plan

If restart causes issues:

```bash
# Kubernetes - rollback
kubectl rollout undo deployment/product-service

# Docker - use previous image
docker-compose up -d product-service:v1.2.0
```

---

## Common Issues

### Service Won't Start

**Check**:
- Database connectivity
- Kafka connectivity
- Configuration errors
- Port conflicts

### Service Starts But Unhealthy

**Check**:
- Database migrations
- Required dependencies
- Resource limits

---

**Last Updated**: 2026-01-19
