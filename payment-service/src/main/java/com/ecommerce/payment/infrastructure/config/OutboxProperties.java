package com.ecommerce.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "outbox")
@Getter
@Setter
public class OutboxProperties {
    private Poll poll = new Poll();
    private Cleanup cleanup = new Cleanup();

    @Getter
    @Setter
    public static class Poll {
        private long intervalMs = 1000;
    }

    @Getter
    @Setter
    public static class Cleanup {
        private String cron = "0 0 3 * * *";
    }
}
