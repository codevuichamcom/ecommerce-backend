# AI Rules & Workflows

> **Nguyên tắc cốt lõi**: Human owns decisions – AI supports thinking & execution

---

## 📁 Cấu trúc

```
.agent/
├── README.md           # Tài liệu này
├── rules/              # 10 Quy tắc kỹ thuật
│   ├── architecture.yaml
│   ├── database.yaml
│   ├── security.yaml
│   ├── testing.yaml
│   ├── api.yaml
│   ├── code_quality.yaml
│   ├── messaging.yaml
│   ├── git.yaml                 # ✨ NEW
│   ├── ci_cd.yaml               # ✨ NEW
│   └── observability.yaml       # ✨ NEW
└── workflows/          # 9 Quy trình làm việc chuẩn
    ├── feature.md
    ├── bugfix.md
    ├── refactor.md
    ├── new_service.md
    ├── hotfix.md                # ✨ NEW
    ├── database_migration.md    # ✨ NEW
    ├── release.md               # ✨ NEW
    ├── pr.md                    # ✨ NEW
    └── dependency_update.md     # ✨ NEW
```

---

## 🎯 Mục đích

Tài liệu này giúp chuẩn hóa toàn bộ quy trình phát triển microservices, đảm bảo AI tuân thủ các nguyên tắc thiết kế, an mật và quy trình vận hành production.

---

## 📋 Rules (Quy tắc)

### 1-7. Core Rules
- **Architecture**: Hexagonal, DDD-lite, SRP.
- **Database**: PostgreSQL 16, Flyway, Optimized JPA.
- **Security**: JWT, Input validation, No hardcoded secrets.
- **Testing**: JUnit 5, Mockito, JaCoCo (Service 70%, Domain 80%).
- **API**: RESTful, RFC 7807, Idempotency, Versioning.
- **Code Quality**: Java 21 features, Record, Sealed classes.
- **Messaging**: Kafka, Outbox, Idempotent Consumer, Saga.

### 8. [git.yaml](rules/git.yaml) ✨
- Git Flow, Conventional Commits, Smash Merge.

### 9. [ci_cd.yaml](rules/ci_cd.yaml) ✨
- Pipeline stages, Canary/Blue-Green deployment, Rollback.

### 10. [observability.yaml](rules/observability.yaml) ✨
- Micrometer Tracing, Structured JSON Logging, Runbooks.

---

## 🔄 Workflows (Quy trình)

### 1. Core Development
- [/feature](workflows/feature.md): Quy trình phát triển tính năng mới (Có feature flags).
- [/bugfix](workflows/bugfix.md): Quy trình sửa lỗi production/staging.
- [/refactor](workflows/refactor.md): Quy trình cải tiến code (Đo lường bằng Complexity).
- [/new_service](workflows/new_service.md): Tạo microservice mới chuẩn skeleton.

### 2. Operational & Quality ✨
- [/hotfix](workflows/hotfix.md): Phản ứng nhanh sự cố P0/P1.
- [/database_migration](workflows/database_migration.md): Thay đổi schema an toàn (Expand-Contract).
- [/release](workflows/release.md): Quy trình release version mới.
- [/pr](workflows/pr.md): Chuẩn PR description và checklist review.
- [/dependency_update](workflows/dependency_update.md): Quản lý thư viện an toàn.

---

## 🚀 Tài liệu chi tiết
Xem tài liệu đầy đủ và slash commands tại: [docs/AI_RULES_AND_WORKFLOWS.md](../docs/AI_RULES_AND_WORKFLOWS.md)

---
**Cập nhật lần cuối**: 2026-01-19
**Version**: 2.3
