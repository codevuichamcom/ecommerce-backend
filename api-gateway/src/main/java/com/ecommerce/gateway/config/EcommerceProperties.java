package com.ecommerce.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.Map;

/**
 * Configuration properties for Ecommerce application.
 * This class helps IDE to recognize custom properties in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "ecommerce")
@Data
public class EcommerceProperties {
    private Map<String, String> services;
    private String jwtSecret;
}
