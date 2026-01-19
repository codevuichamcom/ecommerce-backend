# AI Rules & Workflows - Ecommerce Backend

> **Nguyên tắc cốt lõi**: Con người sở hữu quyết định - AI hỗ trợ tư duy & thực thi

---

## 📋 Tổng quan

Tài liệu này tổng hợp toàn bộ **Rules** (Quy tắc) và **Workflows** (Quy trình làm việc) được thiết lập cho dự án **ecommerce-backend**. Các quy tắc và quy trình này đảm bảo AI assistant tuân thủ các best practices, kiến trúc, và quy trình phát triển của dự án microservices production-ready.

---

## 🎯 Cấu trúc thư mục

```
.agent/
├── rules/              # 10 quy tắc kỹ thuật
│   ├── git.yaml                 # ✨ NEW
│   ├── ci_cd.yaml               # ✨ NEW
│   ├── observability.yaml       # ✨ NEW (Updated: Micrometer Tracing & Runbooks)
│   ├── api.yaml                 # ⚡ ENHANCED
│   ├── security.yaml            # ⚡ ENHANCED
│   ├── testing.yaml             # ⚡ ENHANCED
│   ├── messaging.yaml           # ⚡ ENHANCED
│   ├── architecture.yaml
│   ├── database.yaml
│   └── code_quality.yaml
└── workflows/          # 9 quy trình làm việc
    ├── hotfix.md                # ✨ NEW
    ├── database_migration.md    # ✨ NEW
    ├── release.md               # ✨ NEW
    ├── pr.md                    # ✨ NEW
    ├── dependency_update.md     # ✨ NEW
    ├── feature.md               # ⚡ ENHANCED (Feature Flags)
    ├── bugfix.md
    ├── new_service.md
    └── refactor.md              # ⚡ ENHANCED (Quality Metrics)
```

---

## 📐 Rules (Quy tắc)

### 1. Git & Branching ([git.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/git.yaml))

**Mục đích**: Quy chuẩn Git Flow, branch naming, commit messages, và PR standards.

**Workflow**: Git Flow.

**Merge strategy**: Squash (Feature/Bugfix), Merge commit (Hotfix/Release).

---

### 2. CI/CD Pipeline ([ci_cd.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/ci_cd.yaml))

**Mục đích**: Build pipeline, deployment strategies, environment promotion, rollback.

**Deployment strategies**: Dev (Rolling), Staging (Blue-Green), Production (Canary).

---

### 3. Observability ([observability.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/observability.yaml)) ⚡ UPDATED

**Mục đích**: Monitoring, logging, tracing, alerting cho production.

**Pillars**: Metrics (Micrometer), Logs (Structured JSON), Traces (**Micrometer Tracing**).

**Alerting & Runbooks** ✨:
- Mỗi alert phải đi kèm **Runbook**.
- **Example Runbook**: [sample-alert-runbook.md](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/docs/runbooks/sample-alert-runbook.md).
- **Runbook Template** bao gồm: alert_name, severity, impact, diagnosis_steps, resolution_steps, escalation_path.

---

### 4. API Design ([api.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/api.yaml))

**Mục đích**: RESTful conventions, error handling, versioning, pagination, rate limiting.

**Pagination**: Cursor-based (ưu tiên) hoặc Offset-based.

---

### 5. Security ([security.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/security.yaml))

- **Auth**: JWT + Spring Security.
- **CORS & Headers**: Cấu hình bảo mật nâng cao.
- **Secrets**: AWS Secrets Manager với rotation chu kỳ.

---

### 6. Testing ([testing.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/testing.yaml))

- **Coverage**: ≥70% (service), ≥80% (domain).
- **Advanced**: Contract (Pact), Performance (Gatling), Chaos (Chaos Monkey).

---

### 7. Messaging ([messaging.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/messaging.yaml))

- **Patterns**: Outbox, Idempotent Consumer, Saga.
- **Evolution**: Avro + Schema Registry.

---

### 8. Architecture & Code Quality

- **Kiến trúc**: Hexagonal + DDD-lite.
- **Java 21**: record, sealed class, pattern matching, virtual threads.

---

## 🔄 Workflows (Quy trình làm việc)

### 1. Feature Development ([/feature](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/feature.md))

**Khi nào**: Phát triển tính năng mới. Hỗ trợ **Feature Flags** cho gradual rollout.

---

### 2. Hotfix ([/hotfix](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/hotfix.md))

**Khi nào**: Sự cố Production P0/P1. Quy trình phản ứng nhanh và Post-Mortem.

---

### 4. Database Migration ([/database-migration](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/database_migration.md))

**Khi nào**: Thay đổi database schema. Sử dụng Expand-Contract pattern.

---

### 4. Release ([/release](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/release.md))

**Khi nào**: Release version mới. Quy trình staging QA và Canary deploy.

---

### 5. Dependency Update ([/dependency-update](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/dependency_update.md)) ✨ NEW

**Khi nào**: Quản lý thư viện (Security scan, license check, tech review).

---

### 6. Refactor ([/refactor](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/refactor.md))

**Khi nào**: Cải thiện code quality. Đo lường bằng Complexity và Quality Gate metrics.

---

## 🎓 Best Practices Summary

- ✅ **Git**: Conventional Commits + Git Flow.
- ✅ **Tech**: Java 21 + Hexagonal + Kafka.
- ✅ **Ops**: Canary Deployment + SLOs + Runbooks.
- ✅ **Security**: Secrets Manager + Daily Dependency Scan.

---

## 📞 Slash Commands

| Command | Workflow |
|---------|----------|
| `/feature` | Feature development |
| `/bugfix` | Bug fixing |
| `/hotfix` | Production incident response |
| `/database-migration` | Database schema change |
| `/release` | Production release |
| `/pr` | Create pull request |
| `/new-service` | Create microservice |
| `/refactor` | Refactoring |
| `/dependency-update` | Library management |

---

**Last Updated**: 2026-01-19  
**Version**: 2.3 (Perfect Consistency)  
**Status**: ✅ 10/10 Gold Standard
