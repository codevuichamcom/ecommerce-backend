plugins {
    `java-library`
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
}
