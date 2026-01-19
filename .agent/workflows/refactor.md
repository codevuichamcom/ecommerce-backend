---
description: Refactoring workflow - behavior must not change
---

# Refactor Workflow

## 🚨 Critical Rule
**Behavior MUST NOT change during refactoring.**

## Steps

### 1. Identify Code Smell
- Common smells: God Class, Long Method (>50 lines), Duplicated Code, Magic Numbers, Deep Nesting, Primitive Obsession.

### 2. Verify Safety Net
- Check existing test coverage: `./gradlew test jacocoTestReport`.
- **Required coverage**: Service layer ≥70%, Domain layer ≥80%.
- **If below threshold → Write tests FIRST before refactoring.**

### 3. Plan & Approve ⏸️
- Describe changes and how behavior is preserved.
- **Human Approval**: Mandatory for large refactors or architecture shifts.

### 4. Execute Incrementally
- Make small, logical commits (one logical change per commit).
- Run tests after EVERY change: `./gradlew test`.
- Use IDE refactoring tools (safe rename, extract method, etc.).

### 5. Verify Results
- All tests must pass, coverage must not decrease.
- **Metrics check**:
  - Cyclomatic Complexity (Lower is better).
  - Cognitive Complexity (Lower is better).
  - SonarQube Quality Gate (Must pass).

## Best Practices
- ✅ "Refactor when you're not in a hurry."
- ✅ "The best refactoring starts with deleting code."
- ✅ "If it's hard to test, it's hard to refactor. Fix testing first."

## Checklist
- [ ] Code smell identified and documented
- [ ] Existing test coverage verified (Service ≥70%, Domain ≥80%)
- [ ] Refactor plan documented & approved (if large)
- [ ] Refactoring done in small, incremental steps
- [ ] Tests passed after each step
- [ ] Final quality metrics verified
- [ ] No regression in business behavior
