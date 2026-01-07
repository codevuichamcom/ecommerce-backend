# Stage 1: Extraction
# Use JRE slim to extract layers from the fat JAR
FROM eclipse-temurin:21-jre-alpine AS builder
ARG SERVICE_NAME
WORKDIR /application
# Copy the built jar from the host machine (assumes ./gradlew build has been run)
COPY ${SERVICE_NAME}/build/libs/*.jar application.jar
# Extract layers using Spring Boot's layertools
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2: Final Image
# Use JRE slim for the final image to optimize size
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# Create a new user to run the application (Security Best Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the extracted layers from the builder stage
# The order of COPY commands is important to optimize Docker Cache
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

# Port will be mapped dynamically via docker-compose or environment variables
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
