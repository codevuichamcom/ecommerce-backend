package com.ecommerce.payment.application.service;

import com.ecommerce.common.events.PaymentEvents;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ecommerce.payment.infrastructure.config.PaymentProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Payment application service.
 * Handles payment processing logic.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String AGGREGATE_TYPE = "Payment";

    private final PaymentRepository paymentRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentProperties paymentProperties;
    private final InternalPaymentService internalPaymentService;
    private final Random random = new Random();

    public PaymentService(
            PaymentRepository paymentRepository,
            OutboxEventPublisher outboxEventPublisher,
            PaymentProperties paymentProperties,
            InternalPaymentService internalPaymentService) {
        this.paymentRepository = paymentRepository;
        this.outboxEventPublisher = outboxEventPublisher;
        this.paymentProperties = paymentProperties;
        this.internalPaymentService = internalPaymentService;
    }

    /**
     * Process a payment for an order.
     * This is triggered by Kafka events from the order service.
     * ARCH-002: Refactored to separate transactional steps from long-running
     * processing.
     */
    public PaymentResponse processPayment(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        // 1. Transactional: Check idempotency and initialize payment
        Payment payment = internalPaymentService.initializePayment(command);
        if (!(payment.getStatus() instanceof com.ecommerce.payment.domain.model.PaymentStatus.Processing)) {
            return PaymentResponse.from(payment);
        }

        // 2. Non-Transactional: Long-running processing simulation (outside
        // transaction)
        try {
            simulatePaymentProcessing();

            // Random failure for testing
            if (random.nextDouble() < paymentProperties.getProcessing().getSimulatedFailureRate()) {
                throw new RuntimeException("Simulated payment failure");
            }

            // 3. Transactional: Complete payment and publish event
            payment = internalPaymentService.completePayment(payment.getId());
            log.info("Payment {} completed for order {}", payment.getId().value(), command.orderId());

        } catch (Exception e) {
            // 4. Transactional: Handle failure and publish event
            String errorCode = "PAYMENT_FAILED";
            String reason = e.getMessage() != null ? e.getMessage() : "Unknown error";
            payment = internalPaymentService.failPayment(payment.getId(), reason, errorCode);
            log.error("Payment {} failed for order {}: {}", payment.getId().value(), command.orderId(), reason);
        }

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String id) {
        Payment payment = paymentRepository.findById(new PaymentId(id))
                .orElseThrow(() -> new NotFoundException("Payment", id));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment for order", orderId));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional
    public PaymentResponse refundPayment(String id, String reason) {
        Payment payment = paymentRepository.findById(new PaymentId(id))
                .orElseThrow(() -> new NotFoundException("Payment", id));

        String refundTransactionId = "RFND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.refund(refundTransactionId);
        payment = paymentRepository.save(payment);

        // Publish refund event
        var event = PaymentEvents.PaymentRefunded.create(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getAmount(),
                reason);
        outboxEventPublisher.publish(AGGREGATE_TYPE, payment.getId().value(), event);

        log.info("Payment {} refunded: {}", id, reason);

        return PaymentResponse.from(payment);
    }

    private void simulatePaymentProcessing() {
        try {
            Thread.sleep(paymentProperties.getProcessing().getDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
