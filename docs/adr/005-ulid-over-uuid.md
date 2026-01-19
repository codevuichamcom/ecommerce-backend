# ADR-005: ULID over UUID for Entity IDs

**Status**: Accepted  
**Date**: 2026-01-16  
**Deciders**: Tech Lead, Senior Developers  
**Technical Story**: Entity identifier design

---

## Context

We needed to choose an identifier format for entities (Orders, Products, etc.) that would be:
- Globally unique
- Sortable by creation time
- URL-safe
- Performant for database indexing

### Problem Statement

Traditional auto-increment IDs don't work well in distributed systems. UUIDs are unique but random, making them poor for database indexes and impossible to sort by creation time.

---

## Decision

We will use **ULID (Universally Unique Lexicographically Sortable Identifier)** for entity IDs.

---

## Considered Options

### Option 1: Auto-increment IDs

**Pros**: Simple, sequential, small size  
**Cons**: Not globally unique, coordination required, predictable

### Option 2: UUID v4 (Random)

**Pros**: Globally unique, no coordination  
**Cons**: Random order, poor index performance, not sortable

### Option 3: ULID

**Pros**: Globally unique, sortable, good index performance, URL-safe  
**Cons**: Slightly larger than UUID, less common

---

## Decision Outcome

**Chosen Option**: ULID

**Justification**:
- **Sortable**: Can order by ID to get chronological order
- **Performance**: Better database index performance than UUID
- **Unique**: 128-bit random component ensures uniqueness
- **Readable**: Base32 encoding is URL-safe and readable

---

## Consequences

### Positive
- IDs are sortable by creation time
- Better database index performance
- No need for separate `created_at` index
- URL-safe (no special characters)
- Human-readable (vs UUID)

### Negative
- Less common than UUID (team learning)
- Slightly larger string representation (26 chars vs 36 for UUID)
- Reveals creation time (minor security concern)

---

## Implementation

**ULID Format**:
```
01HQZX3Y4Z5A6B7C8D9E0F1G2H
```

**Structure**:
- 10 characters: Timestamp (milliseconds since epoch)
- 16 characters: Random

**Java Implementation**:
```java
import de.huxhorn.sulky.ulid.ULID;

public class UlidGenerator {
    private static final ULID ulid = new ULID();
    
    public static String generate() {
        return ulid.nextULID();
    }
}
```

**Database Column**:
```sql
CREATE TABLE orders (
    id VARCHAR(26) PRIMARY KEY,  -- ULID
    ...
);
```

**Example Usage**:
```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    
    @Id
    @Column(length = 26)
    private String id;
    
    @PrePersist
    void generateId() {
        if (id == null) {
            id = UlidGenerator.generate();
        }
    }
}
```

---

## Validation

### Success Criteria
- ✅ IDs are globally unique
- ✅ IDs are sortable by creation time
- ✅ Database index performance acceptable
- ✅ URL-safe (no encoding needed)

### Metrics
- **Uniqueness**: No collisions in production
- **Performance**: Index scan time < UUID
- **Sortability**: `ORDER BY id` equals `ORDER BY created_at`

---

## Related Decisions

- [ADR-004: PostgreSQL per Service](004-postgresql-per-service.md)

---

## References

- [ULID Specification](https://github.com/ulid/spec)
- [ULID vs UUID](https://blog.daveallie.com/ulid-primary-keys)
- [Java ULID Library](https://github.com/azam/ulidj)

---

**Last Updated**: 2026-01-19
