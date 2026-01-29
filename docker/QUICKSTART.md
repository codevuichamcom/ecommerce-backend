# 🚀 QUICKSTART - Chạy trong 2 phút!

## Bước 1: Chạy Docker Compose

```bash
cd docker
docker-compose up -d
```

## Bước 2: Đợi services khởi động

Lần đầu sẽ mất 5-10 phút để:
- Download images
- Build các services
- Khởi tạo databases

Kiểm tra status:
```bash
docker-compose ps
```

Xem logs:
```bash
docker-compose logs -f
```

## ✅ Kiểm tra khi nào chạy xong?

Truy cập các URL sau để kiểm tra:

1. **API Gateway Health**: http://localhost:8080/actuator/health
2. **Zipkin UI**: http://localhost:9411
3. **Grafana**: http://localhost:3000 (admin/admin)

Khi tất cả trả về status "UP" hoặc hiển thị UI => ✅ Thành công!

## 🎯 Service URLs

| Service | Health Check |
|---------|-------------|
| API Gateway | http://localhost:8080/actuator/health |
| Auth Service | http://localhost:8086/actuator/health |
| Product Service | http://localhost:8081/actuator/health |
| Inventory Service | http://localhost:8082/actuator/health |
| Order Service | http://localhost:8083/actuator/health |
| Payment Service | http://localhost:8084/actuator/health |
| Notification Service | http://localhost:8085/actuator/health |

## 📊 Monitoring Tools

- **Zipkin** (Tracing): http://localhost:9411
- **Prometheus** (Metrics): http://localhost:9090
- **Grafana** (Dashboards): http://localhost:3000

## 🛑 Dừng & Dọn dẹp

```bash
# Dừng tất cả
docker-compose down

# Dừng và xóa data
docker-compose down -v
```

## 🐛 Troubleshooting

### Lỗi port đã được sử dụng?
Kiểm tra port nào bị conflict:
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Service không start?
Xem logs chi tiết:
```bash
docker-compose logs <service-name>
# Ví dụ:
docker-compose logs auth-service
```

### Out of memory?
Tăng memory cho Docker Desktop lên ít nhất 8GB:
- Docker Desktop → Settings → Resources → Memory

### Build lỗi?
Rebuild từ đầu:
```bash
docker-compose build --no-cache
docker-compose up -d
```

## 📝 Lưu ý

1. **Lần đầu chạy sẽ lâu** (5-10 phút) vì phải build
2. **Lần sau sẽ nhanh hơn** vì đã có cache
3. **Cần ít nhất 8GB RAM** cho Docker
4. **Cổng 8080-8086, 9090-9092, 9411, 3000, 5432, 6379** phải available

## 🎉 Test API

Sau khi services đã UP, test API:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Register user (via API Gateway)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

## 🔧 Helper Script

Sử dụng script helper (Linux/Mac):

```bash
chmod +x run.sh

./run.sh start    # Start services
./run.sh stop     # Stop services
./run.sh logs     # View logs
./run.sh status   # Check status
./run.sh dev      # Development mode
```

---

**Cần help?** Đọc file [README.md](README.md) để biết thêm chi tiết!
