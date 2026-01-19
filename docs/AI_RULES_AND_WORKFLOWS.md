# AI Rules & Workflows - Ecommerce Backend

> **Nguyên tắc cốt lõi**: Con người sở hữu quyết định - AI hỗ trợ tư duy & thực thi

---

## 📋 Tổng quan

Tài liệu này tổng hợp toàn bộ **Rules** (Quy tắc), **Workflows** (Quy trình), và **Templates** (Mẫu) được thiết lập cho dự án **ecommerce-backend**. Hệ thống đã được tối ưu hóa (Version 3.0) để đảm bảo AI có context ngắn gọn nhưng đầy đủ templates khi thực thi các task phức tạp.

---

## 🎯 Cấu trúc thư mục

```
.agent/
├── README.md           # Tóm tắt nhanh
├── rules/              # 10 Quy tắc kỹ thuật ngắn gọn (~50 dòng/file)
├── workflows/          # 9 Quy trình thực thi (Steps & Checklists)
└── templates/          # ✨ NEW - Các mẫu docs, PR, SQL, Runbooks
```

---

## 📐 Rules (10 quy tắc chi tiết)

1. **Git & Branching**: Git Flow, Conventional Commits. ([git.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/git.yaml))
2. **CI/CD Pipeline**: Build, scan, Canary deployment. ([ci_cd.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/ci_cd.yaml))
3. **Observability**: Metrics, JSON logs, Micrometer Tracing. ([observability.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/observability.yaml))
4. **API Design**: RESTful, RFC 7807, Idempotency. ([api.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/api.yaml))
5. **Security**: JWT, AWS Secrets, Snyk scans. ([security.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/security.yaml))
6. **Testing**: JUnit 5, JaCoCo, Testcontainers. ([testing.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/testing.yaml))
7. **Messaging**: Kafka, Outbox, Saga, Avro. ([messaging.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/messaging.yaml))
8. **Architecture**: Hexagonal + DDD-lite. ([architecture.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/architecture.yaml))
9. **Database**: PostgreSQL 16, Flyway. ([database.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/database.yaml))
10. **Code Quality**: Java 21 features, Lombok. ([code_quality.yaml](file:///home/sotatek/Develop/My_Self/Mordern_Java/ecommerce-backend/.agent/rules/code_quality.yaml))

---

## 🔄 Workflows (9 quy trình chi tiết)

- `/feature`: Hỗ trợ Feature Flags, Architecture Proposal.
- `/bugfix`: Reproduce với regression test.
- `/hotfix`: Phản ứng sự cố P0/P1, Post-Mortem.
- `/database_migration`: Expand-Contract pattern cho zero-downtime.
- `/release`: Staging QA, Canary deploy.
- `/pr`: Check kiến trúc, logic và coverage.
- `/new_service`: Skeleton chuẩn theo Hexagonal.
- `/dependency_update`: Quản lý thư viện, quét CVE.
- `/refactor`: Tối ưu code quality metrics.

---

## 📄 Templates (Mẫu tài liệu)

AI sẽ tự động sử dụng các mẫu trong `.agent/templates/` khi thực hiện các workflow tương ứng:
- `pr_template.md`, `release_plan_template.md`, `migration_plan_template.md`, `post_mortem_template.md`, `runbook_template.md`, `service_design_template.md`, etc.

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
**Version**: 3.0 (Context Optimized)  
**Status**: 🏆 Platinum Standard
