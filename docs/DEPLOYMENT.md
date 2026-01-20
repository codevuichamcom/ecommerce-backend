# 🚀 Deployment Guide

Complete guide for deploying the E-commerce Backend to different environments.

---

## Table of Contents

1. [Deployment Overview](#deployment-overview)
2. [Environment Configuration](#environment-configuration)
3. [CI/CD Pipeline](#cicd-pipeline)
4. [Deployment Procedures](#deployment-procedures)
5. [Rollback Procedures](#rollback-procedures)
6. [Health Checks](#health-checks)

---

## Deployment Overview

### Deployment Strategy

| Environment | Strategy | Trigger | Approvers |
|-------------|----------|---------|-----------|
| **Development** | Rolling Update | Auto (merge to `develop`) | - |
| **Staging** | Blue-Green | Manual | Tech Lead, QA Lead |
| **Production** | Canary | Manual | Tech Lead, Product Owner |

### Environment Promotion Flow

```
develop branch → Development → Staging → Production
     ↓              ↓            ↓          ↓
  feature/*      Auto Deploy  Manual    Manual
                              (approval) (approval)
```

### Versioning

**Semantic Versioning**: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

**Tag Format**: `v1.2.3`

---

## Environment Configuration

### Development Environment

**Infrastructure**:
- Single instance per service
- Shared PostgreSQL database
- Local Kafka cluster
- No load balancer

**Configuration**:
```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://dev-db:5432/product_db
  kafka:
    bootstrap-servers: dev-kafka:9092

logging:
  level:
    com.ecommerce: DEBUG
```

**Deployment**:
```bash
# Automatic on merge to develop
git checkout develop
git merge feature/new-feature
git push origin develop
# CI/CD pipeline auto-deploys
```

---

### Staging Environment

**Infrastructure**:
- 2 instances per service (Blue-Green)
- Dedicated PostgreSQL (replica of prod schema)
- Kafka cluster (3 brokers)
- Load balancer (NGINX)

**Configuration**:
```yaml
# application-staging.yml
spring:
  profiles:
    active: staging
  datasource:
    url: jdbc:postgresql://staging-db:5432/product_db
    hikari:
      maximum-pool-size: 20
  kafka:
    bootstrap-servers: staging-kafka-1:9092,staging-kafka-2:9092,staging-kafka-3:9092

management:
  tracing:
    sampling:
      probability: 0.5  # 50% sampling

logging:
  level:
    com.ecommerce: INFO
```

**Deployment**:
```bash
# Manual deployment with approval
# 1. Create release branch
git checkout -b release/v1.2.0 develop

# 2. Tag version
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin v1.2.0

# 3. Trigger deployment (GitHub Actions)
# Requires approval from Tech Lead + QA Lead
```

---

### Production Environment

**Infrastructure**:
- 3+ instances per service (Canary deployment)
- PostgreSQL cluster (Primary + 2 Replicas)
- Kafka cluster (5 brokers, replication factor 3)
- Load balancer (AWS ALB / NGINX)
- CDN for static assets

**Configuration**:
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://prod-db-primary:5432/product_db
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
  kafka:
    bootstrap-servers: prod-kafka-1:9092,prod-kafka-2:9092,prod-kafka-3:9092,prod-kafka-4:9092,prod-kafka-5:9092
    producer:
      acks: all
      retries: 3

management:
  tracing:
    sampling:
      probability: 0.1  # 10% sampling
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    root: WARN
    com.ecommerce: INFO

payment:
  processing:
    simulated-failure-rate: 0.0  # Disable simulation
```

**Deployment**:
```bash
# Manual deployment with strict approval
# 1. Merge release to main
git checkout main
git merge release/v1.2.0
git push origin main

# 2. Trigger canary deployment
# Requires approval from Tech Lead + Product Owner

# 3. Monitor canary metrics (10% traffic for 30 minutes)
# 4. If healthy, promote to 100%
# 5. If issues, automatic rollback
```

---

## CI/CD Pipeline

### Pipeline Tool

**GitHub Actions** (recommended)

### Build Pipeline Stages

```yaml
# .github/workflows/build.yml
name: Build and Test

on:
  push:
    branches: [develop, main, release/*]
  pull_request:
    branches: [develop, main]

jobs:
  compile:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Build
        run: ./gradlew clean build -x test
      
  unit-test:
    needs: compile
    runs-on: ubuntu-latest
    steps:
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Check Coverage
        run: ./gradlew jacocoTestCoverageVerification
        # Fails if coverage < 70%
      
  integration-test:
    needs: unit-test
    runs-on: ubuntu-latest
    steps:
      - name: Run Integration Tests
        run: ./gradlew integrationTest
      
  static-analysis:
    needs: compile
    runs-on: ubuntu-latest
    steps:
      - name: SonarQube Scan
        run: ./gradlew sonarqube
      - name: Checkstyle
        run: ./gradlew checkstyleMain
      
  security-scan:
    needs: compile
    runs-on: ubuntu-latest
    steps:
      - name: OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze
      - name: Snyk Scan
        run: snyk test --severity-threshold=high
      
  docker-build:
    needs: [unit-test, integration-test, static-analysis, security-scan]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop' || github.ref == 'refs/heads/main'
    steps:
      - name: Build Docker Images
        run: docker-compose build
      - name: Scan Images with Trivy
        run: trivy image ecommerce/product-service:latest
      - name: Push to Registry
        run: docker-compose push
```

### Deployment Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to deploy'
        required: true
        type: choice
        options:
          - development
          - staging
          - production

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: ${{ github.event.inputs.environment }}
    steps:
      - name: Deploy to ${{ github.event.inputs.environment }}
        run: |
          # Deployment script
          ./scripts/deploy.sh ${{ github.event.inputs.environment }}
```

---

## Deployment Procedures

### Pre-Deployment Checklist

- [ ] All tests passing (unit + integration)
- [ ] Code review approved
- [ ] Security scans passed
- [ ] Database migrations tested
- [ ] Configuration reviewed
- [ ] Rollback plan prepared
- [ ] Stakeholders notified
- [ ] Monitoring dashboards ready

### Deployment Steps

#### 1. Build Docker Images

```bash
# Build all services
docker-compose build

# Tag with version
docker tag ecommerce/product-service:latest ecommerce/product-service:v1.2.0

# Push to registry
docker push ecommerce/product-service:v1.2.0
```

#### 2. Database Migration Procedures

##### Automated Migrations (Flyway)
The services use **Flyway** for database migrations. By default, migrations are executed automatically on service startup.

**Configuration**:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    out-of-order: false # Ensure migrations run in order
```

**Migration Workflow**:
1. Add new migration script to `src/main/resources/db/migration/V{VERSION}__{DESCRIPTION}.sql`.
2. Commit and push script.
3. CI/CD pipeline validates migration against a staging database instance.
4. On deployment, the service applies migrations during the "Pre-start" phase.

##### Manual Migrations
For complex migrations (e.g., data transformation) or high-load environments, run migrations manually before service deployment.

```bash
# Run Flyway migrate via Gradle (specified for product-service)
./gradlew :product-service:flywayMigrate \
  -Dflyway.url=jdbc:postgresql://prod-db:5432/product_db \
  -Dflyway.user=postgres \
  -Dflyway.password=$DB_PASSWORD
```

##### Migration Failure & Rollback
If a migration fails, Flyway locks the `flyway_schema_history` table.

**Recovery Steps**:
1. Identify the failing script: `SELECT * FROM flyway_schema_history WHERE success = false;`
2. Fix the script or the data issue in the DB.
3. Repair Flyway metadata: `./gradlew flywayRepair`
4. Re-run migration: `./gradlew flywayMigrate`

**Rollback Procedure**:
- **Backward compatible**: Deploy previous code version.
- **Breaking changes**: Restore from point-in-time backup (see [Disaster Recovery](runbooks/disaster-recovery.md)).
- **Manual Undo**: Run the corresponding `U{VERSION}` script if available.

---

#### 3. Service Scaling & Capacity Planning

##### Scaling Strategy
All services are designed to be stateless and scaled horizontally.

**Horizontal Pod Autoscaler (HPA)** (Target):
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: product-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: product-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

##### Resource Allocation
| Tier | CPU Request | CPU Limit | Memory Request | Memory Limit |
|------|-------------|-----------|----------------|--------------|
| **Gateway** | 500m | 1000m | 512Mi | 1Gi |
| **Business Apps** | 1000m | 2000m | 1Gi | 2Gi |
| **Kafka/DB** | 2000m+ | 4000m+ | 4Gi+ | 8Gi+ |

##### Capacity Planning
- **Throughput**: Each service instance is benchmarked to handle ~200 RPS.
- **Storage**: DB storage should have 20% headroom always. 
- **Monitoring**: Alert when CPU/Memory utilization reaches 80% for > 5 minutes.

---

#### 4. Security Audit Checklist

| Item | Status | Tool |
|------|--------|------|
| **JWT Secrets Rotation** | Check every 90 days | Vault / AWS Secrets Manager |
| **Dependency Scanning** | Every build | OWASP / Snyk |
| **Image Vulnerabilities** | Once/week | Trivy / GCR Scan |
| **Heads/Logs Sanitization** | Periodic Manual Review | ELK / CloudWatch |
| **Privileged Access** | Monthly Review | IAM / K8s RBAC |
| **Network Isolation** | Daily Audit | Calico / VPC Flow Logs |

---

#### 5. Deploy Services

**Rolling Update** (Development):
```bash
# Update one instance at a time
kubectl set image deployment/product-service product-service=ecommerce/product-service:v1.2.0
kubectl rollout status deployment/product-service
```

**Blue-Green** (Staging):
```bash
# Deploy to green environment
kubectl apply -f k8s/staging/green/

# Test green environment
curl https://staging-green.example.com/actuator/health

# Switch traffic to green
kubectl patch service product-service -p '{"spec":{"selector":{"version":"green"}}}'

# Monitor for 15 minutes
# If stable, decommission blue
```

**Canary** (Production):
```bash
# Deploy canary (10% traffic)
kubectl apply -f k8s/prod/canary/

# Monitor metrics for 30 minutes
# - Error rate < 1%
# - Latency p95 < 500ms
# - No critical errors

# If healthy, increase to 50%
kubectl scale deployment/product-service-canary --replicas=5

# Monitor for 30 minutes

# If healthy, promote to 100%
kubectl apply -f k8s/prod/stable/
kubectl delete deployment product-service-canary
```

#### 4. Verify Deployment

```bash
# Check service health
curl https://api.example.com/actuator/health

# Check all instances
kubectl get pods -l app=product-service

# Check logs
kubectl logs -f deployment/product-service

# Check metrics
curl https://api.example.com/actuator/prometheus | grep http_server_requests
```

---

## Rollback Procedures

### Automatic Rollback Triggers

- Error rate > 5%
- Latency p95 > 2000ms
- Health check failing
- Critical exceptions

### Manual Rollback

**Kubernetes**:
```bash
# Rollback to previous version
kubectl rollout undo deployment/product-service

# Rollback to specific revision
kubectl rollout undo deployment/product-service --to-revision=2

# Check rollout history
kubectl rollout history deployment/product-service
```

**Docker Compose**:
```bash
# Redeploy previous version
docker-compose up -d product-service:v1.1.0
```

### Database Rollback

```bash
# Restore from backup
psql -h prod-db -U postgres product_db < backup_20260119.sql

# Or run down migration (if available)
./gradlew :product-service:flywayUndo
```

---

## Health Checks

### Liveness Probe

Checks if service is running:

```yaml
# Kubernetes
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 3
```

### Readiness Probe

Checks if service is ready to accept traffic:

```yaml
# Kubernetes
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 5
  failureThreshold: 3
```

### Health Check Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Overall health |
| `/actuator/health/liveness` | Liveness check |
| `/actuator/health/readiness` | Readiness check |
| `/actuator/health/db` | Database health |
| `/actuator/health/kafka` | Kafka health |
| `/actuator/health/redis` | Redis health |

---

## Monitoring Post-Deployment

### Key Metrics to Watch

**First 15 minutes**:
- Error rate
- Response time (p50, p95, p99)
- Request rate
- CPU/Memory usage

**First hour**:
- Database connection pool
- Kafka consumer lag
- Cache hit rate
- Business metrics (orders created, payments processed)

### Grafana Dashboards

Access: https://grafana.example.com

**Dashboards**:
- Service Health Overview
- JVM Metrics
- Database Performance
- Kafka Consumer Lag
- Business Metrics

### Alerts

**Critical Alerts** (PagerDuty):
- Service down
- Error rate > 5%
- Database connection pool exhausted

**Warning Alerts** (Slack):
- High latency (p95 > 1000ms)
- Kafka consumer lag > 1000
- High memory usage (> 80%)

---

## Secrets Management

### AWS Secrets Manager

```bash
# Store secret
aws secretsmanager create-secret \
  --name prod/ecommerce/jwt-secret \
  --secret-string "your-secret-key"

# Retrieve in application
spring:
  cloud:
    aws:
      secretsmanager:
        enabled: true
        prefix: prod/ecommerce
```

### Kubernetes Secrets

```bash
# Create secret
kubectl create secret generic jwt-secret \
  --from-literal=JWT_SECRET=your-secret-key

# Use in deployment
env:
  - name: JWT_SECRET
    valueFrom:
      secretKeyRef:
        name: jwt-secret
        key: JWT_SECRET
```

---

## Infrastructure as Code

### Terraform

```hcl
# terraform/main.tf
resource "aws_db_instance" "product_db" {
  identifier = "ecommerce-product-db"
  engine     = "postgres"
  engine_version = "16"
  instance_class = "db.t3.medium"
  allocated_storage = 100
  
  vpc_security_group_ids = [aws_security_group.db.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
}
```

---

## Deployment Best Practices

### ✅ Do

- **Fail fast**: Run quick tests first
- **Cache dependencies**: Speed up builds
- **Always have rollback plan**: Test rollback procedure
- **Deploy during low traffic**: Minimize impact
- **Monitor actively**: Watch metrics for 1 hour post-deployment
- **Communicate**: Notify stakeholders before/after deployment

### ❌ Don't

- **Manual deployments to production**: Always use CI/CD
- **Skip tests**: Never skip tests to speed up pipeline
- **Deploy on Friday afternoon**: Avoid weekend incidents
- **Deploy multiple services simultaneously**: Deploy one at a time
- **Ignore warnings**: Investigate all warnings before deploying

---

## Troubleshooting Deployment Issues

### Build Failures

```bash
# Clean build
./gradlew clean build --refresh-dependencies

# Check for compilation errors
./gradlew compileJava --stacktrace
```

### Docker Build Failures

```bash
# Build with verbose output
docker-compose build --no-cache --progress=plain

# Check Dockerfile syntax
docker build -f Dockerfile .
```

### Deployment Failures

```bash
# Check pod status
kubectl describe pod product-service-xxx

# Check logs
kubectl logs product-service-xxx

# Check events
kubectl get events --sort-by='.lastTimestamp'
```

---

## Next Steps

- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common deployment issues
- [Observability Guide](OBSERVABILITY.md) - Monitoring and alerting
- [Runbooks](runbooks/) - Incident response procedures
- [Configuration Reference](CONFIGURATION.md) - Environment variables

---

**Document Version**: 1.0  
**Last Updated**: 2026-01-20  
**Maintained By**: DevOps Team
```
