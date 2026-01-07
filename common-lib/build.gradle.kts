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
}

