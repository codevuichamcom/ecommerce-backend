package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Notification Service Application.
 * Handles sending notifications (email, SMS, push) for e-commerce events.
 */
@SpringBootApplication(scanBasePackages = { "com.ecommerce.notification", "com.ecommerce.common" })
@EnableJpaRepositories(basePackages = { "com.ecommerce.notification", "com.ecommerce.common" })
@EntityScan(basePackages = { "com.ecommerce.notification", "com.ecommerce.common" })
@EnableScheduling
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
