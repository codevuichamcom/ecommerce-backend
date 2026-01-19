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

#### Restore from Backup (EBS/S3)
1.  **Stop application instances** to prevent writes.
2.  **Create new DB instance** from the latest snapshot or restore from S3.
3.  **Perform restoration**:
```bash
# Restore via pg_restore (Parallel)
pg_restore -h prod-db-new -U postgres -j 4 -d product_db product_db_full.dump
```
4.  **Update application connection strings** (via Secrets Manager).
5.  **Start application instances** one by one.

#### Point-in-Time Recovery (PITR)
Used when data was corrupted but we need to recover to a time *just before* the incident.
1.  Locate the transaction ID or timestamp of the corruption.
2.  Initiate PITR in AWS RDS / GCP Cloud SQL console selecting the target timestamp.
3.  Verify record consistency.

---

### 2. Kafka Cluster Recovery

#### Scenario: Broker Failure
If a broker fails, partitions with replicas will automatically elect a new leader.
**Recovery**:
- Spin up a new broker instance with the same ID.
- Kafka will automatically sync data from other replicas.

#### Scenario: Complete Cluster Loss
1.  Deploy a new Kafka cluster.
2.  Create all topics (see [Event Catalog](../architecture/event-catalog.md)).
3.  **Data Replay**: Since events are stored in the DB (Outbox table), use the `OutboxRelay` service to re-read and re-publish events from the last 24 hours.

---

### 3. Service Regional Failover

Used when an entire AWS/Azure/GCP region goes down.
1.  **Switch Traffic** via Route53 / Global Accelerator to the backup region.
2.  **Scale up** backup instances (HPA will handle this, but manual override is safer).
3.  **Promote Read Replica** in the backup region to Primary.

---

## Recovery Verification & Post-Mortem

### 1. Data Consistency Check
Verify that the `total_amount` in `orders` matches the sum of transaction amounts in `payments` for the last 1000 records.

### 2. Monitoring Baseline
Confirm that Prometheus/Grafana show normal traffic patterns after failover.

### 3. Incident Communication Plan
- **Status Page**: `https://status.ecommerce.com` updated every 15 mins.
- **Internal**: `#war-room` Slack channel.

---

## Backup Strategy Summary

| Component | Method | Frequency | Retention |
|-----------|--------|-----------|-----------|
| **PostgreSQL** | RDS Snapshot + WAL | Daily / 5 min | 35 days |
| **Kafka** | Disk Snapshots | Daily | 7 days |
| **Configuration** | Git / Vault Versions | On change | Indefinite |
| **Docker Images** | Registry Versioning | Every build | 90 days / 10 tags |

---

## Post-Recovery Flow
1. **Stabilize**: Monitor system for 4 hours.
2. **Backfill**: Run scripts to reconcile any missing data from logs.
3. **Formal Post-Mortem**: Document within 48 hours.

---

## Emergency Contacts

- **On-Call Engineer**: PagerDuty
- **Database Admin**: @dba-team
- **Security Team**: @security
- **CTO**: emergency-only

---

**Last Updated**: 2026-01-19
**Maintained By**: DevOps Team
