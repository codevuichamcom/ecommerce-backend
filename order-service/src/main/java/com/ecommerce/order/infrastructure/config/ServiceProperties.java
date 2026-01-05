package com.ecommerce.order.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for external services.
 * This class helps resolve IDE warnings about unknown properties.
 */
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {

    private ServiceUrl product;
    private ServiceUrl inventory;

    public ServiceUrl getProduct() {
        return product;
    }

    public void setProduct(ServiceUrl product) {
        this.product = product;
    }

    public ServiceUrl getInventory() {
        return inventory;
    }

    public void setInventory(ServiceUrl inventory) {
        this.inventory = inventory;
    }

    public static class ServiceUrl {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
