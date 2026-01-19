---
description: Create pull request
---

# Pull Request Workflow

## Overview
This workflow guides you through creating a well-documented pull request that's easy to review and merge.

---

## Prerequisites

Before creating a PR, ensure:
- [ ] All changes committed to feature/bugfix branch
- [ ] Tests written and passing
- [ ] Code follows project conventions
- [ ] Documentation updated (if needed)

---

## Steps

### 1. Update Your Branch

```bash
# Fetch latest changes
git checkout develop
git pull origin develop

# Rebase your feature branch
git checkout feature/ECOM-123-your-feature
git rebase develop

# Resolve conflicts if any
# Then continue
git rebase --continue

# Force push (your feature branch only!)
git push origin feature/ECOM-123-your-feature --force-with-lease
```

### 2. Run Final Checks

```bash
# Run all tests
./gradlew test

# Check coverage
./gradlew test jacocoTestReport

# Build
./gradlew build

# Verify no lint errors
./gradlew check
```

### 3. Create Pull Request

**PR Title Format**: `[{Type}] {Ticket-ID}: {Description}`

**Examples**:
- `[Feature] ECOM-123: Add payment service`
- `[Bugfix] ECOM-456: Fix inventory race condition`
- `[Refactor] ECOM-789: Extract email validation to value object`

**PR Description Template**:
```markdown
## Description
Brief description of what this PR does.

## Type of Change
- [ ] Feature
- [ ] Bug Fix
- [ ] Hotfix
- [ ] Refactoring
- [ ] Documentation
- [ ] Performance Improvement

## Changes Made
- **product-service**: Added description field to Product entity
- **product-service**: Created Flyway migration V2
- **common-lib**: Added EmailValidator utility

## API Changes
- ✅ Backward compatible
- OR: ⚠️ Breaking change: [describe]

## Database Changes
- Added `description` column to `products` table
- Created index `idx_products_description`

## Testing Done
- [ ] Unit tests added/updated (coverage: 85%)
- [ ] Integration tests added/updated
- [ ] Manual testing completed
- [ ] All tests passing locally

**Test Coverage**:
- Service layer: 90%
- Domain layer: 95%

## Screenshots/Videos
(If UI changes, add screenshots or videos)

## Checklist
- [ ] Code follows project conventions
- [ ] Self-reviewed the code
- [ ] Commented complex code sections
- [ ] Updated documentation
- [ ] No new warnings
- [ ] Tests achieve required coverage (≥70%)
- [ ] Reviewed against all rules in `.agent/rules/`

## Related Tickets
Refs: ECOM-123

## Breaking Changes
None

## Deployment Notes
- No special deployment steps needed
- OR: Requires database migration - see `docs/migrations/ECOM-123-migration-plan.md`

## Rollback Plan
- Revert this PR
- OR: Run rollback migration `V2_rollback.sql`
```

### 4. Request Reviewers

**Reviewer assignment**:
- **Feature**: 1 reviewer (peer engineer)
- **Bugfix**: 1 reviewer
- **Hotfix**: 2 reviewers
- **Refactor**: 2 reviewers
- **Breaking change**: 2+ reviewers including Tech Lead
- **Database migration**: Tech Lead + DBA

**Labels to add**:
- `feature`, `bugfix`, `hotfix`, `refactor`
- `needs-review`
- `priority:high` (if urgent)
- Service label: `product-service`, `order-service`, etc.

### 5. Address Review Feedback

**When reviewer requests changes**:

```bash
# Make changes locally
# Commit changes
git add .
git commit -m "Address review feedback: extract validation logic"

# Push to update PR
git push origin feature/ECOM-123-your-feature
```

**Respond to comments**:
- ✅ "Fixed in commit abc123"
- ✅ "Good catch! Changed from X to Y"
- ✅ "Added unit test for this case"
- ❌ Don't just mark as resolved without explanation

### 6. Merge Pull Request

**After approval**:

1. **Ensure CI passes** (all checks green)
2. **Squash and merge** (for feature/bugfix to develop)
3. **Use meaningful squash message**:
   ```
   feat(product-service): add product description field (#123)
   
   - Added description column to products table
   - Created Flyway migration V2
   - Updated ProductDTO and ProductEntity
   - Added unit and integration tests
   
   Refs: ECOM-123
   ```

4. **Delete branch** after merge

```bash
# Delete local branch
git branch -d feature/ECOM-123-your-feature

# Delete remote branch (usually done automatically by GitHub/GitLab)
git push origin --delete feature/ECOM-123-your-feature
```

---

## PR Size Guidelines

**Ideal PR size**: 200-400 lines changed

**Why?**
- Easier to review
- Faster to merge
- Lower risk
- Easier to revert if needed

**If your PR is large (>500 lines)**:
- Consider splitting into multiple PRs
- Or: Add detailed description and comments
- Or: Schedule synchronous code review

---

## Self-Review Checklist

Before requesting review, check:

### Code Quality
- [ ] No commented-out code
- [ ] No debug print statements
- [ ] No TODOs without ticket reference
- [ ] Meaningful variable/method names
- [ ] No magic numbers

### Testing
- [ ] Happy path tested
- [ ] Error cases tested
- [ ] Edge cases tested
- [ ] Integration tests if cross-service
- [ ] Coverage ≥70%

### Documentation
- [ ] Complex logic commented
- [ ] Public APIs documented
- [ ] README updated if new feature
- [ ] Migration plan if database change

### Security
- [ ] No hardcoded secrets
- [ ] Input validation added
- [ ] No sensitive data in logs
- [ ] Authentication/authorization checked

### Performance
- [ ] No N+1 queries
- [ ] Indexes added if new queries
- [ ] No unnecessary database calls
- [ ] Caching considered

---

## Common PR Mistakes

❌ **Don't**:
- Create PR without description
- Mix multiple unrelated changes
- Commit large files (binaries, dependencies)
- Force push after review started (unless rebasing)
- Merge without CI passing
- Merge your own PR without review

✅ **Do**:
- Keep PR focused on one thing
- Write clear commit messages
- Respond to review comments
- Keep PR up to date with base branch
- Test locally before pushing
- Be open to feedback

---

## Reviewer Guidelines

**As a reviewer, check**:

### Correctness
- Does it solve the problem?
- Are there bugs?
- Are edge cases handled?

### Design
- Is the design sound?
- Does it fit the architecture?
- Is it maintainable?

### Testing
- Are tests comprehensive?
- Do tests actually test the logic?
- Is coverage adequate?

### Code Quality
- Follows conventions?
- Readable and clear?
- Well-documented?

**Review etiquette**:
- ✅ Be constructive and respectful
- ✅ Ask questions, don't demand
- ✅ Praise good code
- ✅ Explain your suggestions
- ❌ Don't be nitpicky about style (use linter)
- ❌ Don't approve if you don't understand

---

## Example Checklist

- [ ] Branch updated with latest develop
- [ ] All tests passing locally
- [ ] PR created with proper title and description
- [ ] Reviewers assigned
- [ ] Labels added
- [ ] Self-review completed
- [ ] Review feedback addressed
- [ ] CI checks passing
- [ ] PR approved
- [ ] Merged and branch deleted

---

## Useful Commands

```bash
# Check what files changed
git diff develop...feature/ECOM-123-your-feature

# Check commits that will be in PR
git log develop..feature/ECOM-123-your-feature

# Interactive rebase to clean up commits
git rebase -i develop

# View PR diff in terminal
gh pr diff 123  # GitHub CLI

# View PR status
gh pr status

# Checkout PR locally for testing
gh pr checkout 123
```
