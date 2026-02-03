# ============================================================================
# Simple Dockerfile for Spring Boot Microservices
# ============================================================================
# Build: docker build --build-arg SERVICE_NAME=product-service -t product-service .
# ============================================================================

FROM gradle:8.12-jdk21 AS builder

ARG SERVICE_NAME
WORKDIR /app

# Set environment variables for better SSL/TLS handling
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Djavax.net.ssl.trustStoreType=jks"

# Copy gradle wrapper and build files first (better caching)
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Make gradlew executable
RUN chmod +x gradlew

# Pre-download dependencies (Go-style optimization)
# This layer will be cached unless build files change
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew help --no-daemon

# Copy common-lib first (needed by all services)
COPY common-lib common-lib

# Copy shared logback configuration for JSON logging
COPY docker/logback-spring.xml /tmp/logback-spring.xml

# Copy the specific service
COPY ${SERVICE_NAME} ${SERVICE_NAME}

# Copy logback-spring.xml to service resources (will override if exists)
RUN mkdir -p ${SERVICE_NAME}/src/main/resources && \
    cp /tmp/logback-spring.xml ${SERVICE_NAME}/src/main/resources/logback-spring.xml

# Build the service using gradlew wrapper instead of gradle command
# Use cache mount for .gradle folder to speed up subsequent builds
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew :${SERVICE_NAME}:bootJar -x test --no-daemon --stacktrace

# Extract layers
RUN mkdir -p extracted && \
    JAR_FILE=$(find ${SERVICE_NAME}/build/libs -name "*.jar" ! -name "*-plain.jar") && \
    java -Djarmode=layertools -jar "$JAR_FILE" extract --destination extracted

# ============================================================================
# Runtime Stage
# ============================================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget curl

# Create non-root user
RUN addgroup -g 1001 spring && \
    adduser -u 1001 -S spring -G spring

# Copy application layers
COPY --from=builder --chown=spring:spring /app/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/extracted/application/ ./

USER spring:spring

# Don't expose a fixed port - let docker-compose handle it
# Each service will use its own port via SERVER_PORT env var

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
