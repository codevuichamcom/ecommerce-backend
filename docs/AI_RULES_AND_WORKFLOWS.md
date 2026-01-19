# AI Rules & Workflows - Ecommerce Backend

> **Nguyên tắc cốt lõi**: Con người sở hữu quyết định - AI hỗ trợ tư duy & thực thi

---

## 📋 Tổng quan

Tài liệu này tổng hợp toàn bộ **Rules** (Quy tắc) và **Workflows** (Quy trình làm việc) được thiết lập cho dự án **ecommerce-backend**. Các quy tắc và quy trình này đảm bảo AI assistant tuân thủ các best practices, kiến trúc, và quy trình phát triển của dự án microservices production-ready.

---

## 🎯 Cấu trúc thư mục

```
.agent/
├── README.md           # Tóm tắt nhanh
├── rules/              # 10 Quy tắc kỹ thuật
│   ├── git.yaml                 # ✨ NEW
│   ├── ci_cd.yaml               # ✨ NEW
│   ├── observability.yaml       # ✨ NEW
│   ├── api.yaml                 # ⚡ ENHANCED
│   ├── security.yaml            # ⚡ ENHANCED
│   ├── testing.yaml             # ⚡ ENHANCED
│   ├── messaging.yaml           # ⚡ ENHANCED
│   ├── architecture.yaml
│   ├── database.yaml
│   └── code_quality.yaml
└── workflows/          # 9 Quy trình làm việc
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

## 📐 Rules (Quy tắc chi tiết)

### 1. Git & Branching ([git.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/git.yaml))
- **Workflow**: Git Flow (main, develop, feature, bugfix, hotfix, release).
- **Convention**: Conventional Commits (`feat:`, `fix:`, `docs:`, etc.).
- **Standards**: Reference Ticket ID trong footer, Smash merge cho feature.

### 2. CI/CD Pipeline ([ci_cd.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/ci_cd.yaml))
- **Stages**: Compile → Test → Static Analysis (SonarQube) → Docker Build/Scan.
- **Strategies**: Dev (Rolling), Staging (Blue-Green), Production (Canary).
- **Rollback**: Tự động khi error rate > 5% hoặc manual khi có sự cố.

### 3. Observability ([observability.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/observability.yaml))
- **Tracing**: Micrometer Tracing (thay thế Sleuth).
- **Logging**: Structured JSON, mask sensitive data (PII).
- **Alerting**: Gắn kèm Runbook cho mọi alert. [Sample Runbook](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/docs/runbooks/sample-alert-runbook.md).

### 4. API Design ([api.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/api.yaml))
- **Versioning**: Path-based (`/api/v1/...`).
- **Standard**: RFC 7807 Problem Details.
- **Mutations**: Bắt buộc có `Idempotency-Key`.
- **Pagination**: Ưu tiên Cursor-based cho dữ liệu lớn.

### 5. Security ([security.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/security.yaml))
- **Auth**: JWT, Spring Security Roles (ADMIN, CUSTOMER, SERVICE).
- **Secrets**: AWS Secrets Manager, rotation chu kỳ 90 ngày.
- **Scan**: Snyk & OWASP daily scan.

### 6. Testing ([testing.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/testing.yaml))
- **Coverage**: Tool JaCoCo, Service ≥70%, Domain ≥80%.
- **Patterns**: Given-When-Then, Testcontainers cho Integration tests.
- **Advanced**: Contract Testing (Pact), Chaos Engineering (Chaos Monkey).

### 7. Messaging ([messaging.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/messaging.yaml))
- **Pattern**: Outbox, Idempotent Consumer, Saga.
- **Schema**: Avro + Schema Registry (Confluent).
- **Limits**: Max message 1MB, alert khi Lag > 10,000.

### 8. Architecture ([architecture.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/architecture.yaml))
- **Pattern**: Hexagonal Architecture.
- **DDD**: Domain logic tách biệt khỏi JPA Entity.
- **Rule**: No business logic in Controllers or Repositories.

### 9. Database ([database.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/database.yaml))
- **PostgreSQL**: Version 16.
- **Migration**: Flyway (Sequential numbering).
- **Review**: Human review BẮT BUỘC cho mọi thay đổi schema.

### 10. Code Quality ([code_quality.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/code_quality.yaml))
- **Java 21**: Record, Sealed classes, Pattern matching.
- **Lombok**: Hạn chế `@SneakyThrows`.
- **Naming**: PascalCase cho Class, camelCase cho Method.

---

## 🔄 Workflows (Quy trình chi tiết)

### 1. Feature Development ([/feature](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/feature.md))
Quy trình phát triển tính năng từ Requirement → Architecture Review → Code. Có hỗ trợ **Feature Flags** (Toggles).

### 2. Bug Fixing ([/bugfix](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/bugfix.md))
Tập trung vào Reproduce và Regression Test trước khi fix.

### 3. Hotfix ([/hotfix](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/hotfix.md))
Phản ứng nhanh cho sự cố Production, bỏ qua một số bước review rườm rà nhưng yêu cầu Post-Mortem.

### 4. Database Migration ([/database_migration](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/database_migration.md))
Quy trình nâng cấp database không gây downtime bằng Expand-Contract pattern.

### 5. Release ([/release](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/release.md))
Nâng version, cập nhật changelog và triển khai qua Staging → Production (Canary).

### 6. Pull Request ([/pr](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/pr.md))
Chuẩn hóa cách đặt title, mô tả và checklist tự kiểm tra chất lượng code trước khi gửi review.

### 7. New Microservice ([/new_service](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/new_service.md))
Tạo mới một service hoàn chỉnh với cấu trúc thư mục, cấu hình Gradle và Docker chuẩn.

### 8. Dependency Update ([/dependency_update](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/dependency_update.md))
Cập nhật thư viện an toàn: Check license, quét CVE và test độ tương thích.

### 9. Refactor ([/refactor](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/workflows/refactor.md))
Cải tiến code mà không đổi behavior. Đo lường bằng Cyclomatic & Cognitive Complexity.

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
| `/new_service` | Create microservice |
| `/refactor` | Refactoring |
| `/dependency_update` | Library management |

---

**Last Updated**: 2026-01-19  
**Version**: 2.3  
**Status**: ✅ Gold Standard
