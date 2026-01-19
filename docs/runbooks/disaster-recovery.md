# Runbook: Disaster Recovery

**Severity**: Critical  
**MTTR Target**: 60 minutes  
**RTO**: 1 hour | **RPO**: 15 minutes

---

## Disaster Scenarios

1. **Complete Data Center Failure**
2. **Database Corruption**
3. **Ransomware Attack**
4. **Accidental Data Deletion**

---

## Recovery Procedures

### 1. Database Recovery

#### Restore from Backup

```bash
# List available backups
aws s3 ls s3://ecommerce-backups/postgres/

# Download latest backup
aws s3 cp s3://ecommerce-backups/postgres/product_db_20260119.sql.gz .

# Restore database
gunzip product_db_20260119.sql.gz
psql -h prod-db -U postgres product_db < product_db_20260119.sql
```

#### Point-in-Time Recovery

```bash
# PostgreSQL PITR
pg_restore --dbname=product_db \
  --clean \
  --if-exists \
  backup_file.dump
```

### 2. Kafka Topic Recovery

```bash
# Replay from earliest offset
kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group order-service-group \
  --topic order-events \
  --reset-offsets \
  --to-earliest \
  --execute
```

### 3. Service Recovery

```bash
# Deploy to backup region
kubectl config use-context backup-cluster

# Apply all manifests
kubectl apply -f k8s/

# Verify deployment
kubectl get pods --all-namespaces
```

---

## Recovery Verification

### 1. Data Integrity

```sql
-- Check record counts
SELECT COUNT(*) FROM orders;
SELECT COUNT(*) FROM products;

-- Verify latest records
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10;
```

### 2. Service Health

```bash
# Check all services
for port in 8080 8081 8082 8083 8084 8085 8086; do
  curl http://localhost:$port/actuator/health
done
```

### 3. End-to-End Test

```bash
# Create test order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer TOKEN" \
  -d '{"customerId":"test","items":[{"productId":"test-product","quantity":1}]}'
```

---

## Backup Strategy

### Automated Backups

**Databases**:
- Full backup: Daily at 3 AM
- Incremental: Every 6 hours
- Retention: 30 days

**Configuration**:
```bash
# Cron job
0 3 * * * /scripts/backup-databases.sh
```

**Kafka**:
- Topic replication: 3x
- Log retention: 7 days

---

## Communication Plan

### Incident Declaration

1. **Notify**: Post in `#incidents` Slack channel
2. **Page**: Alert on-call via PagerDuty
3. **Escalate**: Notify CTO for major outages

### Status Updates

- Every 15 minutes during recovery
- Use status page for customer communication

---

## Post-Recovery

### 1. Post-Mortem

- Document timeline
- Identify root cause
- Create action items

### 2. Update Runbook

- Note what worked/didn't work
- Update procedures

### 3. Test Recovery

- Schedule quarterly DR drills
- Verify backup integrity

---

## Emergency Contacts

- **On-Call Engineer**: PagerDuty
- **Database Admin**: @dba-team
- **Security Team**: @security
- **CTO**: emergency-only

---

**Last Updated**: 2026-01-19
