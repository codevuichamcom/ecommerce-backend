rootProject.name = "ecommerce-backend"

// Include all subprojects
include(
    "common-lib",
    "api-gateway",
    "auth-service",
    "product-service",
    "inventory-service",
    "order-service",
    "payment-service",
    "notification-service"
)

// Plugin management
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    
    plugins {
        id("org.springframework.boot") version "3.5.9"
        id("io.spring.dependency-management") version "1.1.4"
        kotlin("jvm") version "1.9.23"
        kotlin("plugin.spring") version "1.9.23"
    }
}

// Dependency resolution
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
