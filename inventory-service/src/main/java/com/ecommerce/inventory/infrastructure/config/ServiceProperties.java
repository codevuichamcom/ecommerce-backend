package com.ecommerce.inventory.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for external services.
 * This class helps resolve IDE warnings about unknown properties.
 */
@Data
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {

    private ServiceUrl product = new ServiceUrl();

    @Data
    public static class ServiceUrl {
        /**
         * URL of the external service.
         */
        private String url;
    }
}
