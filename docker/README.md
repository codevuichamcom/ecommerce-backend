# 🚀 E-Commerce Backend - Docker Guide (SIMPLE VERSION)

## ⚡ Quick Start - Chỉ 2 Bước!

### Bước 1: Cấu hình (1 lần duy nhất)
```bash
cd docker
cp .env.example .env
# Sửa .env nếu cần (hoặc để mặc định cũng được)
```

### Bước 2: Chạy!
```bash
docker-compose up -d
```

**Xong! Đợi 5-10 phút lần đầu để build.**

---

## 📋 Các Lệnh Cơ Bản

```bash
# Start tất cả
docker-compose up -d

# Stop tất cả
docker-compose down

# Xem logs
docker-compose logs -f

# Xem logs của 1 service
docker-compose logs -f api-gateway

# Restart
docker-compose restart

# Rebuild sau khi sửa code
docker-compose build
docker-compose up -d

# Xem status
docker-compose ps

# Xóa tất cả (cẩn thận!)
docker-compose down -v
```

---

## 🌐 Truy Cập Services

Sau khi chạy `docker-compose up -d`, truy cập:

### Application
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8086
- **Product Service**: http://localhost:8081
- **Inventory Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **Notification Service**: http://localhost:8085

### Monitoring
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Zipkin**: http://localhost:9411

### Infrastructure
- **PostgreSQL**: localhost:5432 (postgres/postgres)
- **Redis**: localhost:6379
- **Kafka**: localhost:9092

---

## 🔧 Troubleshooting

### Services không start?
```bash
# Xem logs để biết lỗi
docker-compose logs

# Hoặc xem log của service cụ thể
docker-compose logs postgres
docker-compose logs api-gateway
```

### Build lỗi?
```bash
# Rebuild từ đầu
docker-compose build --no-cache
docker-compose up -d
```

### Port bị chiếm?
```bash
# Sửa port trong file .env
# Ví dụ: API_GATEWAY_PORT=8080 → 8090
```

### Out of memory?
- Mở Docker Desktop
- Settings → Resources → Memory
- Tăng lên 8GB
- Apply & Restart

---

## 📝 File Structure

```
docker/
├── docker-compose.yml          # File chính (tất cả ở đây)
├── .env                        # Environment variables
├── .env.example                # Template
├── init-databases.sh           # PostgreSQL init script
└── prometheus/
    └── prometheus.yml          # Prometheus config
```

---

## 💡 Tips

### Development Workflow
```bash
# 1. Start lần đầu
docker-compose up -d

# 2. Sửa code...

# 3. Rebuild service đã sửa
docker-compose build product-service
docker-compose restart product-service

# 4. Xem logs
docker-compose logs -f product-service
```

### Chỉ Start Infrastructure (Nhanh hơn)
```bash
# Chỉ start DB, Redis, Kafka
docker-compose up -d postgres redis kafka zipkin

# Sau đó start service cần thiết
docker-compose up -d product-service
```

### Clean Up
```bash
# Stop và xóa containers
docker-compose down

# Stop, xóa containers + volumes (mất data!)
docker-compose down -v

# Stop, xóa containers + volumes + images
docker-compose down -v --rmi all
```

---

## ⚙️ Configuration

### File .env (Tùy chọn)

```env
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Redis
REDIS_PASSWORD=redis_password

# JWT
JWT_SECRET=your-secret-key-change-this-in-production

# Grafana
GF_SECURITY_ADMIN_PASSWORD=admin

# Kafka
KAFKA_CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk
```

**Mặc định đã OK, chỉ cần đổi khi deploy production!**

---

## 🎯 Tóm Tắt

| Làm Gì | Lệnh |
|--------|------|
| **Start tất cả** | `docker-compose up -d` |
| **Stop tất cả** | `docker-compose down` |
| **Xem logs** | `docker-compose logs -f` |
| **Rebuild** | `docker-compose build` |
| **Status** | `docker-compose ps` |
| **Restart** | `docker-compose restart` |

---

## ✅ Checklist

- [ ] Docker Desktop đã cài và đang chạy
- [ ] File `.env` đã tạo (copy từ `.env.example`)
- [ ] Chạy `docker-compose up -d`
- [ ] Đợi 5-10 phút lần đầu
- [ ] Truy cập http://localhost:8080

**Xong! Đơn giản vậy thôi!** 🎉
