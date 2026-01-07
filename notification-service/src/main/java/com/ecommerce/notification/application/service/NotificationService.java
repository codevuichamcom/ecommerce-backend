package com.ecommerce.notification.application.service;

import com.ecommerce.notification.application.dto.NotificationResponse;
import com.ecommerce.notification.application.dto.SendNotificationCommand;
import com.ecommerce.notification.domain.model.*;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.notification.infrastructure.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Notification application service.
 * Handles sending and managing notifications.
 */
@Service
@Transactional
public class NotificationService {

        private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

        private final NotificationRepository notificationRepository;
        private final EmailSender emailSender;

        public NotificationService(
                        NotificationRepository notificationRepository,
                        EmailSender emailSender) {
                this.notificationRepository = notificationRepository;
                this.emailSender = emailSender;
        }

        /**
         * Send a notification.
         */
        public NotificationResponse sendNotification(SendNotificationCommand command) {
                log.info("Sending notification to {}: type={}", command.recipientId(), command.notificationType());

                NotificationType type = NotificationType.valueOf(command.notificationType());

                Notification notification = Notification.createEmail(
                                command.recipientId(),
                                command.recipientEmail(),
                                type,
                                command.subject(),
                                command.message(),
                                command.metadata());

                notification = notificationRepository.save(notification);

                // Send the notification
                try {
                        String messageId = emailSender.sendEmail(
                                        notification.getRecipientEmail(),
                                        notification.getSubject(),
                                        notification.getMessage());
                        notification.markSent(messageId);
                        notification = notificationRepository.save(notification);
                        log.info("Notification {} sent successfully: messageId={}", notification.getId().value(),
                                        messageId);
                } catch (Exception e) {
                        notification.markFailed(e.getMessage());
                        notification = notificationRepository.save(notification);
                        log.error("Failed to send notification {}: {}", notification.getId().value(), e.getMessage());
                }

                return NotificationResponse.from(notification);
        }

        /**
         * Send order confirmation notification.
         */
        public NotificationResponse sendOrderConfirmation(
                        String customerId,
                        String customerEmail,
                        String orderId,
                        String orderAmount) {
                Map<String, String> metadata = Map.of(
                                "orderId", orderId,
                                "amount", orderAmount);

                String message = String.format(
                                "Your order #%s has been confirmed. Total: %s",
                                orderId.substring(0, 8).toUpperCase(),
                                orderAmount);

                return sendNotification(new SendNotificationCommand(
                                customerId,
                                customerEmail,
                                NotificationType.ORDER_CONFIRMED.name(),
                                "Order Confirmed - #" + orderId.substring(0, 8).toUpperCase(),
                                message,
                                metadata));
        }

        /**
         * Send payment received notification.
         */
        public NotificationResponse sendPaymentReceived(
                        String customerId,
                        String customerEmail,
                        String orderId,
                        String amount,
                        String transactionId) {
                Map<String, String> metadata = Map.of(
                                "orderId", orderId,
                                "amount", amount,
                                "transactionId", transactionId);

                String message = String.format(
                                "We have received your payment of %s for order #%s. Transaction ID: %s",
                                amount,
                                orderId.substring(0, 8).toUpperCase(),
                                transactionId);

                return sendNotification(new SendNotificationCommand(
                                customerId,
                                customerEmail,
                                NotificationType.PAYMENT_RECEIVED.name(),
                                "Payment Received",
                                message,
                                metadata));
        }

        /**
         * Send payment failed notification.
         */
        public NotificationResponse sendPaymentFailed(
                        String customerId,
                        String customerEmail,
                        String orderId,
                        String reason) {
                Map<String, String> metadata = Map.of(
                                "orderId", orderId,
                                "reason", reason);

                String message = String.format(
                                "We were unable to process your payment for order #%s. Reason: %s. Please try again or use a different payment method.",
                                orderId.substring(0, 8).toUpperCase(),
                                reason);

                return sendNotification(new SendNotificationCommand(
                                customerId,
                                customerEmail,
                                NotificationType.PAYMENT_FAILED.name(),
                                "Payment Failed",
                                message,
                                metadata));
        }

        /**
         * Send order cancelled notification.
         */
        public NotificationResponse sendOrderCancelled(
                        String customerId,
                        String customerEmail,
                        String orderId,
                        String reason) {
                Map<String, String> metadata = Map.of(
                                "orderId", orderId,
                                "reason", reason);

                String message = String.format(
                                "Your order #%s has been cancelled. Reason: %s",
                                orderId.substring(0, 8).toUpperCase(),
                                reason);

                return sendNotification(new SendNotificationCommand(
                                customerId,
                                customerEmail,
                                NotificationType.ORDER_CANCELLED.name(),
                                "Order Cancelled",
                                message,
                                metadata));
        }

        /**
         * Get notification by ID.
         */
        @Transactional(readOnly = true)
        public NotificationResponse getNotification(String id) {
                return notificationRepository.findById(new NotificationId(id))
                                .map(NotificationResponse::from)
                                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        }

        /**
         * Get notifications for a recipient.
         */
        @Transactional(readOnly = true)
        public List<NotificationResponse> getNotificationsForRecipient(String recipientId) {
                return notificationRepository.findByRecipientId(recipientId).stream()
                                .map(NotificationResponse::from)
                                .toList();
        }
}
