package com.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Payment Service Application.
 * Handles payment processing for orders.
 */
@SpringBootApplication(scanBasePackages = { "com.ecommerce.payment", "com.ecommerce.common" })
@EnableJpaRepositories(basePackages = { "com.ecommerce.payment", "com.ecommerce.common" })
@EntityScan(basePackages = { "com.ecommerce.payment", "com.ecommerce.common" })
@EnableScheduling
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
