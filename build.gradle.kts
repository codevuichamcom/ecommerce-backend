import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.9" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}


// Common configuration for all subprojects
subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    
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
        finalizedBy("jacocoTestReport") // Use string name for stability in Kotlin DSL
    }

    configure<org.gradle.testing.jacoco.plugins.JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/dto/**",
                        "**/entity/**",
                        "**/config/**",
                        "**/*Application*",
                        "**/common/exception/**"
                    )
                }
            })
        )
    }

    tasks.withType<org.gradle.testing.jacoco.tasks.JacocoCoverageVerification> {
        violationRules {
            rule {
                limit {
                    minimum = "0.70".toBigDecimal()
                }
            }
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

// Aggregate JaCoCo report for the whole project
tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoRootReport") {
    group = "verification"
    description = "Generates an aggregate report from all subprojects"

    val subprojects = subprojects
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(subprojects.map { it.tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>() })

    additionalSourceDirs.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    classDirectories.setFrom(subprojects.map { 
        it.tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().map { report -> report.classDirectories }
    })
    executionData.setFrom(subprojects.map { 
        it.fileTree(it.buildDir).include("jacoco/*.exec")
    })

    reports {
        xml.required.set(true)
        xml.outputLocation.set(file("${buildDir}/reports/jacoco/aggregate/report.xml"))
        html.required.set(true)
        html.outputLocation.set(file("${buildDir}/reports/jacoco/aggregate/html"))
    }
}


