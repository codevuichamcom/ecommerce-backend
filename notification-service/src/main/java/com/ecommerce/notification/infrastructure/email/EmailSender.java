package com.ecommerce.notification.infrastructure.email;

/**
 * Interface for sending emails.
 */
public interface EmailSender {

    /**
     * Send an email.
     * 
     * @return Message ID for tracking
     */
    String sendEmail(String to, String subject, String body);
}
