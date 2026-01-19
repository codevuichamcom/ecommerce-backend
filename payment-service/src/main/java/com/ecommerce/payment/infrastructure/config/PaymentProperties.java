package com.ecommerce.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {
    private Processing processing = new Processing();

    @Getter
    @Setter
    public static class Processing {
        private long delayMs = 500;
        private double simulatedFailureRate = 0.1;
    }
}
