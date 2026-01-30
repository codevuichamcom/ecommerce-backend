package com.ecommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Auth Service.
 * 
 * @author ecommerce-team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = { "com.ecommerce.auth", "com.ecommerce.common" })
@EnableJpaRepositories(basePackages = { "com.ecommerce.auth" })
@EntityScan(basePackages = { "com.ecommerce.auth" })
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
