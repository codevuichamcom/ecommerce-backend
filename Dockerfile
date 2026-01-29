# Stage 1: Build & Extract
FROM gradle:jdk21-alpine AS builder
WORKDIR /app
ARG SERVICE_NAME

# Copy configuration files
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties* .

# Copy source code of all modules
COPY common-lib common-lib
COPY api-gateway api-gateway
COPY auth-service auth-service
COPY product-service product-service
COPY inventory-service inventory-service
COPY order-service order-service
COPY payment-service payment-service
COPY notification-service notification-service

# Build using system gradle (avoids ./gradlew permission/CRLF issues)
RUN gradle :${SERVICE_NAME}:bootJar -x test --no-daemon

# Extract layers using Spring Boot's layertools
RUN java -Djarmode=layertools -jar ${SERVICE_NAME}/build/libs/*.jar extract --destination extracted

# Stage 2: Final Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# Create a new user to run the application (Security Best Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the extracted layers from the builder stage
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# Port will be mapped dynamically via docker-compose or environment variables
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

