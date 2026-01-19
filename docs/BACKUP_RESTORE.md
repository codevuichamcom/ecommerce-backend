# Database Backup and Restore

Guide for managing backups and data recovery for all microservice databases.

---

## 📅 Backup Schedule

| DB Type | Snapshot Frequency | Point-in-Time | Retention |
|---------|-------------------|---------------|-----------|
| **Production** | Daily (03:00 UTC) | 5 minutes (WAL) | 35 days |
| **Staging** | Daily (03:00 UTC) | N/A | 7 days |
| **Dev** | Weekly | N/A | 1 day |

---

## 💾 Backup Procedures

### 1. Manual Backup (RDS Snapshot)
```bash
aws rds create-db-snapshot \
    --db-instance-identifier prod-order-db \
    --db-snapshot-identifier prod-order-db-manual-$(date +%Y%m%d)
```

### 2. Manual Export (pg_dump)
```bash
pg_dump -h prod-db-host -U postgres -d order_db -F c -b -v -f order_db.dump
```

---

## 🔄 Restore Procedures

### 1. Restore RDS Snapshot
1.  Navigate to AWS Console → RDS → Snapshots.
2.  Select snapshot and click **Actions** → **Restore**.
3.  Specify new DB instance identifier (e.g., `prod-order-db-restored`).
4.  Update Application secrets to point to the new DB.

### 2. Restore pg_dump File
```bash
pg_restore -h new-db-host -U postgres -d order_db -v order_db.dump
```

---

## 🧪 Verification
Every month, the DevOps team must perform a "Dry Run" restore to verify the integrity of backups.
1. Restore a random snapshot to a temporary instance.
2. Run `SELECT COUNT(*)` on main tables.
3. Verify latest record timestamps match the expected backup window.
