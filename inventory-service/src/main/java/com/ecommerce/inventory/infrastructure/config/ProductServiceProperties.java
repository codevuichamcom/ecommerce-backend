package com.ecommerce.inventory.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "product.service")
public class ProductServiceProperties {
    /**
     * URL of the Product Service used for seeding.
     */
    private String url = "http://localhost:8081";
}
