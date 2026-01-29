# Docker Compose Configuration - Best Practices Applied

This Docker Compose setup has been optimized following 2024-2025 best practices for production-ready microservices deployment.

## 🔐 Security Improvements

### 1. Environment Variables & Secrets Management
All sensitive data has been externalized to environment variables:

```bash
# Copy the example file
cp .env.example .env

# Edit with your actual values
nano .env
```

**Important:** Never commit `.env` to version control!

### 2. Generate Secure Secrets

```bash
# Generate new JWT secret
openssl rand -hex 32

# Generate strong passwords
openssl rand -base64 32
```

## 🚀 Quick Start

### Prerequisites
- Docker Engine 24.0+
- Docker Compose V2
- At least 8GB RAM available

### Start All Services
```bash
# From the docker directory
cd docker

# Create .env file from template
cp .env.example .env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

### Start Infrastructure Only (For Local Development)
```bash
docker-compose up -d postgres redis kafka kafka-ui zipkin prometheus grafana
```

## 📊 Resource Allocation

| Service | CPU Limit | Memory Limit | Purpose |
|---------|-----------|--------------|---------|
| Postgres | 2.0 | 1GB | Database |
| Redis | 1.0 | 512MB | Cache |
| Kafka (KRaft) | 2.0 | 1GB | Message Broker + Metadata |
| Java Services | 1.0 | 512MB | Microservices |
| Prometheus | 1.0 | 1GB | Metrics |
| Grafana | 0.5 | 512MB | Dashboards |

## 🌐 Network Architecture

Three isolated networks:
- **frontend**: API Gateway ↔ External
- **backend**: Services ↔ Databases/Kafka
- **monitoring**: Observability stack

## 🔄 Restart Policies

All services use `restart: unless-stopped` for automatic recovery from failures.

## 📦 Pinned Image Versions

All images use specific versions (no `latest` tags) for reproducibility:
- `postgres:16-alpine`
- `redis:7.2-alpine`
- `apache/kafka:3.7.0` (KRaft Mode)
- `openzipkin/zipkin:3.4`
- `prom/prometheus:v2.53.0`
- `grafana/grafana:11.1.0`

## 🏥 Health Checks

All critical services have health checks configured:
- Postgres: `pg_isready`
- Redis: `redis-cli ping`
- Kafka: Topic listing
- Monitoring: HTTP health endpoints

## 📝 Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_USER` | postgres | Database username |
| `POSTGRES_PASSWORD` | postgres | Database password (⚠️ CHANGE THIS!) |
| `REDIS_PASSWORD` | redis_password | Redis password (⚠️ CHANGE THIS!) |
| `JWT_SECRET` | (provided) | JWT signing key (⚠️ CHANGE THIS!) |
| `GF_SECURITY_ADMIN_PASSWORD` | admin | Grafana admin password |
| `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` | 1.0 | Trace sampling rate (0.0-1.0) |

## 🛠️ Useful Commands

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ DATA LOSS)
docker-compose down -v

# Rebuild specific service
docker-compose up -d --build product-service

# View resource usage
docker stats

# Clean up unused resources
docker system prune -a
```

## 🔍 Monitoring & Debugging

- **Kafka UI**: http://localhost:8090
- **Zipkin**: http://localhost:9411
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/[your-password])
- **API Gateway**: http://localhost:8080

## ⚠️ Production Checklist

Before deploying to production:

- [ ] Change all default passwords
- [ ] Generate new JWT secret
- [ ] Review resource limits for your workload
- [ ] Set up external secret management (Vault, AWS Secrets Manager)
- [ ] Configure backup strategy for volumes
- [ ] Set up log aggregation
- [ ] Review network security rules
- [ ] Enable TLS/SSL for external endpoints
- [ ] Configure monitoring alerts

## 📚 Additional Resources

- [Docker Compose Best Practices](https://docs.docker.com/compose/production/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Microservices Security](https://microservices.io/patterns/security/access-token.html)
