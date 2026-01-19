# Runbook: High Memory Usage / OOM

**Severity**: High  
**Symptom**: Service restarts with `Exit Code 137` (OOM Killed) or `java.lang.OutOfMemoryError`.

---

## 1. Immediate Mitigation

1.  **Restart Instance**: Temporary fix to restore service.
2.  **Rollback Recent Changes**: If OOM started after a specific deployment, rollback immediately.
3.  **Scale Up (Vertical)**: Increase memory limits in K8s/Docker.

---

## 2. Diagnosis

### A. Check GC Metrics
Check `jvm_gc_memory_promoted_bytes_total` in Grafana. If it's rising sharply, you have a leak.

### B. Capture Heap Dump
```bash
jmap -dump:live,format=b,file=oom.hprof <PID>
```
Analyze with **Eclipse Memory Analyzer (MAT)**. Look for "Leak Suspects".

### C. Check Large Queries
Check if the service is loading a huge amount of rows into memory without pagination.

---

## 3. Resolution

### A. Optimization
- Fix N+1 select problems.
- Use `Stream` or `Page` for DB results.
- Reduce cache size if using local memory (Caffeine/Ehcache).

### B. Adjust JVM Args
```bash
-Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

---

## 4. Prevention
1.  **Set Limits**: Always set `Xmx` slightly lower than the container limit to allow for native memory.
2.  **Load Testing**: Run stress tests to identify memory ceilings before production.
