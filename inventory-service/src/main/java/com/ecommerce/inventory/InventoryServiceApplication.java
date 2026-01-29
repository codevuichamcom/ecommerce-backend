package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages = { "com.ecommerce.inventory", "com.ecommerce.common" })
@EnableJpaRepositories(basePackages = { "com.ecommerce.inventory", "com.ecommerce.common" })
@EntityScan(basePackages = { "com.ecommerce.inventory", "com.ecommerce.common" })
@EnableRetry
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
