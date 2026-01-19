---
description: Create pull request
---

# Pull Request Workflow

## Prerequisites
- [ ] All changes committed to feature/bugfix branch.
- [ ] Code follows project conventions.
- [ ] Tests written and passing locally (≥70% coverage).

## Steps

### 1. Update Branch
```bash
git checkout develop && git pull
git checkout {your-branch} && git rebase develop
git push origin {your-branch} --force-with-lease
```

### 2. Run Final Checks
```bash
./gradlew clean build check jacocoTestReport
```

### 3. Create Pull Request
- **Title**: `[{Type}] {Ticket-ID}: {Description}`.
- **Description**: Use the project standard template.
  - Template: [.agent/templates/pr_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/pr_template.md)
- **Reviewers**: Request reviews based on [Reviewer Guidelines](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/reviewer_guidelines.md).

### 4. Merge
- **Squash & Merge**: Preferred for feature/bugfix branches.
- **Cleanup**: Delete branch after successful merge.

## PR Size Guidelines
- **Ideal**: 200-400 lines changed.
- **Large (>500 lines)**: Consider splitting or scheduling a sync review.

## Checklist
- [ ] Branch rebased on latest `develop`.
- [ ] Self-reviewed the code (No debug logs, no magic numbers).
- [ ] PR template filled correctly.
- [ ] All CI checks passing.
- [ ] Approved by required reviewers.
