---
description: Bug fixing workflow with regression test
---

# Bug Fix Workflow

## Overview
This workflow ensures bugs are fixed systematically with proper root cause analysis and regression tests.

---

## Steps

### 1. Reproduce Issue
- Read the bug report carefully
- Understand the expected vs actual behavior
- Reproduce the issue locally if possible
- Identify which service(s) are affected
- Check logs for error messages/stack traces

### 2. Identify Root Cause
- Trace through the code path
- Use debugger if needed
- Check recent changes (git blame, git log)
- Review related tests (are they missing?)
- Find the exact source of the bug

### 3. Propose Fix
- Explain the fix approach clearly
- Consider potential side effects
- Assess impact on other features
- **If critical path or complex fix → Wait for human approval ⏸️**

### 4. Add Regression Test
- Write a test that **fails without the fix**
- Test should cover the exact bug scenario
- Ensure test will catch this bug if it reoccurs
- Place test in appropriate test class

### 5. Implement Fix
- Apply minimal changes to fix the bug
- **Don't refactor unrelated code** (save for separate PR)
- Follow existing code style
- Update comments if behavior changed

### 6. Verify Fix
- Run the new regression test → should pass
- Run all existing tests: `./gradlew test`
- Ensure no tests broke
- Test the specific scenario manually if possible
- Check JaCoCo coverage didn't decrease

---

## Critical Rules

❌ **Don't:**
- Fix multiple unrelated bugs in one PR
- Refactor code while fixing bugs
- Skip writing regression test
- Assume the fix works without testing

✅ **Do:**
- Keep changes minimal and focused
- Write regression test first
- Document why the bug occurred
- Test thoroughly before submitting

---

## Example Checklist

- [ ] Bug reproduced locally
- [ ] Root cause identified
- [ ] Fix approach documented
- [ ] Human approval (if critical)
- [ ] Regression test written
- [ ] Regression test fails before fix
- [ ] Fix implemented
- [ ] Regression test passes after fix
- [ ] All tests passing
- [ ] Manual verification done
- [ ] PR created with bug details
