package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.ecommerce.product", "com.ecommerce.common" })
@EnableJpaRepositories(basePackages = { "com.ecommerce.product", "com.ecommerce.common" })
@EntityScan(basePackages = { "com.ecommerce.product", "com.ecommerce.common" })
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
