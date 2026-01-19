# AI Rules & Workflows

> **Nguyên tắc cốt lõi**: Human owns decisions – AI supports thinking & execution

## 📁 Cấu trúc

```
.agent/
├── README.md           # Tài liệu này
├── rules/              # Quy tắc bắt buộc cho AI
│   ├── architecture.yaml
│   ├── database.yaml
│   ├── security.yaml
│   ├── testing.yaml
│   ├── api.yaml
│   ├── code_quality.yaml
│   └── messaging.yaml
└── workflows/          # Quy trình làm việc chuẩn
    ├── feature.md
    ├── bugfix.md
    ├── refactor.md
    └── new_service.md
```

---

## 🎯 Mục đích

Bộ rules và workflows này giúp:
- **Đảm bảo chất lượng code** khi làm việc với AI
- **Chuẩn hóa quy trình** phát triển
- **Ngăn chặn lỗi phổ biến** khi AI tự động generate code
- **Giữ kiến trúc nhất quán** trong toàn bộ microservices

---

## 📋 Rules (Quy tắc)

### 1. [architecture.yaml](rules/architecture.yaml)
- Hexagonal Architecture (Ports & Adapters)
- Domain-Driven Design (DDD-lite)
- Phân tầng: Application → Domain → Infrastructure
- **Cấm**: Controller truy cập Repository trực tiếp, Business logic trong Controller

### 2. [database.yaml](rules/database.yaml)
- PostgreSQL 16 + Flyway migrations
- JPA best practices (optimistic locking, separate entities)
- **Yêu cầu human review** cho mọi thay đổi schema
- Redis caching

### 3. [security.yaml](rules/security.yaml)
- JWT authentication (auth-service + api-gateway)
- Spring Security
- Input validation
- **Cấm**: Hardcoded secrets, logging sensitive data

### 4. [testing.yaml](rules/testing.yaml)
- JUnit 5 + Mockito
- JaCoCo coverage: Service ≥70%, Domain ≥80%
- Testcontainers cho integration tests
- Naming: `given_when_then`

### 5. [api.yaml](rules/api.yaml)
- RESTful conventions
- RFC 7807 Problem Details cho error responses
- Idempotency-Key cho mutations
- **Cấm**: Breaking changes không có deprecation

### 6. [code_quality.yaml](rules/code_quality.yaml)
- Java 21 features: `record`, `sealed class`, pattern matching, virtual threads
- Lombok, MapStruct
- **Cấm**: Magic numbers, catch generic exception, duplicated logic
- **Yêu cầu review** khi thêm dependency mới

### 7. [messaging.yaml](rules/messaging.yaml)
- Kafka 7.5
- **Bắt buộc**: Outbox pattern, Idempotent consumer, Saga pattern
- Event format: JSON với eventId, eventType, timestamp
- Topic naming: `{domain}.{event-type}`

---

## 🔄 Workflows (Quy trình)

### 1. [/feature](workflows/feature.md) - Feature Development
```
Analyze → Propose Architecture → Human Review ⏸️ → Implement → Test → Self Review → PR
```
**Điểm dừng**: Human review sau khi thiết kế architecture

### 2. [/bugfix](workflows/bugfix.md) - Bug Fixing
```
Reproduce → Root Cause → Propose Fix → Regression Test → Implement → Verify
```
**Điểm dừng**: Human approval nếu critical path

### 3. [/refactor](workflows/refactor.md) - Refactoring
```
Identify Smell → Ensure Tests → Propose Plan → Human Approve ⏸️ → Refactor Incrementally → Verify
```
**Quy tắc vàng**: Behavior MUST NOT change

### 4. [/new-service](workflows/new_service.md) - New Microservice
```
Define Responsibility → API Contract → Domain Model → Human Review ⏸️ → Skeleton → Implement
```
**Điểm dừng**: Human review architecture trước khi code

---

## 🚀 Cách sử dụng

### Với AI Assistant
AI sẽ tự động:
- Tuân thủ rules trong `.agent/rules/`
- Làm theo workflows trong `.agent/workflows/`
- Dừng lại ở các checkpoint để chờ human review

### Với Developer
1. **Trước khi bắt đầu task**: Xem workflow tương ứng
2. **Trong quá trình code**: Tham khảo rules liên quan
3. **Trước khi commit**: Self-review theo checklist trong workflow

---

## ✅ Ví dụ sử dụng

### Khi develop feature mới:
```bash
# AI sẽ follow workflow: /feature
1. Analyze requirement
2. Propose architecture (document in docs/)
3. ⏸️ Wait for human approval
4. Implement (Domain → Application → Infrastructure)
5. Write tests (coverage ≥70%)
6. Self review against rules
7. Create PR
```

### Khi fix bug:
```bash
# AI sẽ follow workflow: /bugfix
1. Reproduce issue
2. Identify root cause
3. Add regression test (fails before fix)
4. Implement minimal fix
5. Verify (test passes, all tests green)
```

---

## 🎓 Nguyên tắc làm việc

### ✅ AI được phép:
- Phân tích code, đề xuất giải pháp
- Generate code theo rules
- Viết tests
- Refactor với test coverage đầy đủ

### ⏸️ AI phải dừng và hỏi:
- Thay đổi DB schema
- Thêm dependency mới
- Breaking API changes
- Architecture decisions
- Refactor lớn

### ❌ AI không được:
- Tự ý sửa schema production
- Thêm library chưa review
- Skip tests
- Hardcode secrets
- Violate hexagonal architecture

---

## 📚 Tài liệu liên quan

- [MASTER_PLAN.md](../docs/MASTER_PLAN.md) - Tổng quan dự án
- [docs/architecture/](../docs/architecture/) - Chi tiết kiến trúc
- [PHASE3_IMPLEMENTATION_PLAN.md](../docs/PHASE3_IMPLEMENTATION_PLAN.md) - Phase hiện tại

---

## 🔧 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5.9 |
| Build | Gradle 8.12 (Kotlin DSL) |
| Database | PostgreSQL 16 + Flyway |
| Cache | Redis 7 |
| Messaging | Apache Kafka 7.5 |
| Gateway | Spring Cloud Gateway |
| Security | JWT + Spring Security |
| Observability | Micrometer + Zipkin + Prometheus |

---

## 📝 Lưu ý

- Rules này được thiết kế dựa trên **best practices** và **pain points thực tế** của dự án
- Không phải "chuẩn global" - được tùy chỉnh cho ecommerce-backend
- Có thể update khi dự án phát triển
- Mọi thay đổi rules cần được team review

---

**Cập nhật lần cuối**: 2026-01-19
