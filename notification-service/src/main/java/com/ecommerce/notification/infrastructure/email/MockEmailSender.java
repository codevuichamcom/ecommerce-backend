package com.ecommerce.notification.infrastructure.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock email sender for development/testing.
 * Logs emails instead of sending them.
 */
@Component
public class MockEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(MockEmailSender.class);

    @Override
    public String sendEmail(String to, String subject, String body) {
        String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("========== MOCK EMAIL ==========");
        log.info("Message ID: {}", messageId);
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("================================");

        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return messageId;
    }
}
