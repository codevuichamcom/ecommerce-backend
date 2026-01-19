# Runbook: High Memory Usage

**Alert Name**: `HighMemoryUsage`  
**Severity**: Warning  
**MTTR Target**: 30 minutes

---

## Alert Details

**Trigger**:
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
```

**Symptoms**:
- Slow response times
- Frequent GC pauses
- OutOfMemoryError

---

## Diagnosis

### 1. Check Memory Usage

```bash
# Prometheus
curl http://localhost:8081/actuator/prometheus | grep jvm_memory

# JVM info
jcmd <PID> VM.native_memory summary
```

### 2. Generate Heap Dump

```bash
# Create heap dump
jmap -dump:format=b,file=heap.bin <PID>

# Analyze with Eclipse MAT or VisualVM
```

### 3. Check for Memory Leaks

Common causes:
- Caching without eviction
- Large result sets
- Connection/resource leaks

---

## Resolution

### Option 1: Increase Heap Size

```bash
# Run with more memory
java -Xmx2g -Xms1g -jar service.jar
```

### Option 2: Fix Memory Leaks

```java
// ❌ Bad - Unbounded cache
private Map<String, Product> cache = new HashMap<>();

// ✅ Good - Bounded cache with eviction
@Cacheable(value = "products", key = "#id")
public Product findById(String id) { ... }
```

### Option 3: Enable Pagination

```java
// ❌ Bad - Load all
List<Product> products = productRepository.findAll();

// ✅ Good - Paginate
Page<Product> products = productRepository.findAll(PageRequest.of(0, 20));
```

---

## Verification

```bash
# Check memory usage decreased
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```

---

**Last Updated**: 2026-01-19
