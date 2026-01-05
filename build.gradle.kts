import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "3.5.9" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

// Common configuration for all subprojects
subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    
    group = "com.ecommerce"
    version = "1.0.0-SNAPSHOT"
    
    // Java 21 configuration
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    
    repositories {
        mavenCentral()
    }
    
    // Dependency management - Spring Boot BOM
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.9")
        }
    }
    
    // Common dependencies for all subprojects
    dependencies {
        // Lombok
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")
        "testCompileOnly"("org.projectlombok:lombok")
        "testAnnotationProcessor"("org.projectlombok:lombok")
        
        // Testing
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.assertj:assertj-core")
    }
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf(
            "-parameters",  // Preserve parameter names
            "--enable-preview"  // Enable preview features
        ))
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("--enable-preview")
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

// Task to run all services
tasks.register("bootRunAll") {
    group = "application"
    description = "Run all microservices"
    dependsOn(
        ":product-service:bootRun",
        ":inventory-service:bootRun",
        ":order-service:bootRun"
    )
}
