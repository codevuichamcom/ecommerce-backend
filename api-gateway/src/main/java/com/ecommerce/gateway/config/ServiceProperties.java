package com.ecommerce.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "ecommerce.services")
@Getter
@Setter
public class ServiceProperties {
    private String productUrl = "http://localhost:8081";
    private String inventoryUrl = "http://localhost:8082";
    private String orderUrl = "http://localhost:8083";
    private String paymentUrl = "http://localhost:8084";
    private String notificationUrl = "http://localhost:8085";
    private String authUrl = "http://localhost:8086";
}
