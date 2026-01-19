---
description: Production release workflow
---

# Release Workflow

## Overview
Guides the process of releasing a new version to production (Major, Minor, or Patch).

## Steps

### 1. Preparation
- **Release Plan**: Create planning doc in `docs/releases/`.
  - Template: [.agent/templates/release_plan_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/release_plan_template.md)
- **Verify**: Ensure all features merged to `develop`, tests passing, and docs updated.

### 2. Release Branch
```bash
git checkout develop && git pull
git checkout -b release/{version} && git push origin release/{version}
```

### 3. Versioning & Changelog
- **Version**: Update in `build.gradle.kts` and `docker-compose.yml`.
- **Changelog**: Update `CHANGELOG.md`.
  - Template: [.agent/templates/changelog_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/changelog_template.md)
- **Commit**: `chore: prepare release {version}`.

### 4. Staging QA
- **Deploy**: Run Flyway migrations and deploy to staging.
- **Test**: Run functional, integration, performance (k6), and security scans.

### 5. Release PR
- **Target**: `release/{version}` → `main`.
- **Title**: `[Release] v{version}`.
- **Approvals**: 2+ (Tech Lead, QA Lead, PO).

### 6. Production Deployment
- **Merge**: Squash and merge to `main`, tag version.
- **Sync**: Merge `main` back to `develop`.
- **Deploy**: Run Flyway and deploy (Rolling or Canary).
  - See: [ci_cd.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/ci_cd.yaml)

### 7. Verification & Announcement
- **Smoke test**: Check `/actuator/health` and core API endpoints.
- **Announce**: Internal (Slack) and Customer-facing.
  - Template: [.agent/templates/announcement_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/announcement_template.md)

### 8. Post-Release
- **Release Notes**: Create GitHub Release.
- **Retrospective**: Conduct review within 1 week.
  - Template: [.agent/templates/retrospective_template.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/templates/retrospective_template.md)

## Checklist
- [ ] Release plan approved
- [ ] Staging QA passed
- [ ] Release PR approved & CI green
- [ ] Production deployment verified
- [ ] Announcement sent
- [ ] Retrospective scheduled
