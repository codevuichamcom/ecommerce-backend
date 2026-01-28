plugins {
    `java-library`
    id("io.spring.dependency-management")
}

description = "Common library - shared utilities, DTOs, and base classes"

dependencies {
    // Validation
    api("jakarta.validation:jakarta.validation-api")
    
    // Jackson for JSON
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // ULID for ID generation
    api("com.github.f4b6a3:ulid-creator:5.2.3")
    
    // Spring Kafka (for outbox poller and event handling)
    api("org.springframework.kafka:spring-kafka")
    
    // Spring Transaction (for @Transactional support)
    api("org.springframework:spring-tx")
    
    // Spring Context (for @Scheduled support)
    api("org.springframework:spring-context")
    
    // SLF4J for logging
    api("org.slf4j:slf4j-api")

    // Web (for filters and web context)
    api("org.springframework:spring-web")
    api("org.springframework.security:spring-security-core")
    api("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // Phase 4: Resilience4j
    api("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    api("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
    api("io.github.resilience4j:resilience4j-retry:2.2.0")
    api("io.github.resilience4j:resilience4j-bulkhead:2.2.0")
    api("io.github.resilience4j:resilience4j-timelimiter:2.2.0")
    api("io.github.resilience4j:resilience4j-micrometer:2.2.0")

    // Testing
    testImplementation("org.junit.platform:junit-platform-launcher")
}
