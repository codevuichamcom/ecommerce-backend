---
description: Refactoring workflow - behavior must not change
---

# Refactor Workflow

## Critical Rule
🚨 **Behavior MUST NOT change during refactoring**

---

## Overview
This workflow ensures safe refactoring with comprehensive test coverage and incremental changes.

---

## Steps

### 1. Identify Code Smell
Common code smells to refactor:
- **God Class**: Class doing too many things
- **Long Method**: Method > 50 lines
- **Duplicated Code**: Same logic in multiple places
- **Magic Numbers**: Hardcoded values without constants
- **Deep Nesting**: Too many nested if/for statements
- **Primitive Obsession**: Using primitives instead of value objects

Document what needs improvement and why.

### 2. Ensure Test Coverage
- Check existing tests cover the code to refactor
- Run: `./gradlew test jacocoTestReport`
- View coverage report: `build/reports/jacoco/test/html/index.html`
- **If coverage < 70% → Write tests FIRST before refactoring**
- Tests act as safety net during refactoring

### 3. Propose Refactor Plan
- Describe what changes will be made
- Explain how behavior is preserved
- List steps for incremental refactoring
- Identify potential risks

### 4. Human Approve ⏸️
**STOP HERE for large refactors**
- Get approval before proceeding
- Discuss alternative approaches
- Confirm scope is appropriate

### 5. Refactor Incrementally
- Make small commits (one logical change per commit)
- Run tests after each change: `./gradlew test`
- Ensure `./gradlew build` passes after each commit
- If tests fail → revert and try different approach
- Use IDE refactoring tools when possible (safer)

### 6. Final Verification
- All tests must pass
- Coverage should not decrease
- Behavior is identical to before
- Code is cleaner and more maintainable
- No new warnings or errors

**Quality Metrics Verification**:
| Metric | Before | After | Target |
|--------|--------|-------|--------|
| Cyclomatic Complexity | X | X-N | Lower is better |
| Cognitive Complexity | Y | Y-M | Lower is better |
| SonarQube Quality Gate | Status | Status | Must Pass |

---

## Refactoring Techniques

### Extract Method
```java
// Before
public void processOrder(Order order) {
    // 50 lines of code
}

// After
public void processOrder(Order order) {
    validateOrder(order);
    reserveInventory(order);
    createPayment(order);
}
```

### Extract Value Object
```java
// Before
public class Order {
    private String customerEmail;
}

// After
public record Email(String value) {
    public Email {
        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}

public class Order {
    private Email customerEmail;
}
```

### Replace Magic Number
```java
// Before
if (order.getItems().size() > 10) { ... }

// After
private static final int MAX_ITEMS_PER_ORDER = 10;
if (order.getItems().size() > MAX_ITEMS_PER_ORDER) { ... }
```

---

## Example Checklist

- [ ] Code smell identified and documented
- [ ] Existing test coverage verified (≥70%)
- [ ] Additional tests written if needed
- [ ] Refactor plan created
- [ ] Human approval received (for large refactors)
- [ ] Refactoring done incrementally
- [ ] Tests pass after each change
- [ ] Final build successful
- [ ] Behavior unchanged
- [ ] Code quality improved
- [ ] PR created
