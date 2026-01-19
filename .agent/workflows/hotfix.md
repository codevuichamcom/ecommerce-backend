---
description: Production incident response for P0/P1 bugs
---

# Hotfix Workflow

## ⚡ Critical Rule
**For PRODUCTION INCIDENTS ONLY (P0/P1).**
- P0: Total outage / Data corruption.
- P1: Major feature broken / Performance > 50% drop.

## Steps

### 1. Incident Declaration
- Create incident ticket (INC-XXX).
- Notify stakeholders (Tech Lead, PO).
- For P0: Start incident call with Tech Lead & SRE.

### 2. Decision: Rollback vs Hotfix
- **Rollback**: Best if recent deployment caused clear issue.
  ```bash
  kubectl rollout undo deployment/{service} -n prod
  ```
- **Hotfix**: Use if rollback is unsafe or issue is not deployment-related.

### 3. Implementation (Hotfix)
- **Branch**: From `main`.
- **Focus**: Minimal change to restore service. No refactoring.
- **Test**: Regression test preferred, local verification mandatory.

### 4. Expedited Deployment
- **PR**: `[HOTFIX P0] INC-XXX: {Description}`.
- **Review**: 2 reviewers (priority: immediate).
- **Tag**: Create and push version tag.
- **Merge**: Merge to `main` AND back to `develop`.

### 5. Post-Incident
- **Notify**: Resolution update to stakeholders.
- **Post-Mortem**: Document within 24 hours.
  - Template: [.agent/templates/post_mortem_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/post_mortem_template.md)

## Checklist
- [ ] Incident ticket created
- [ ] Decision signed off (Rollback vs Hotfix)
- [ ] Hotfix reviewed by 2 engineers
- [ ] Merged to `main` and back to `develop`
- [ ] Resolution verified in production
- [ ] Post-mortem created & action items tracked
