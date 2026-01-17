package com.ecommerce.payment.application.service;

import com.ecommerce.common.events.PaymentEvents;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String AGGREGATE_TYPE = "Payment";

    private final PaymentRepository paymentRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();

    @Value("${payment.processing.delay-ms:500}")
    private long processingDelayMs;

    @Value("${payment.processing.simulated-failure-rate:0.1}")
    private double simulatedFailureRate;

    public PaymentService(
            PaymentRepository paymentRepository,
            OutboxEventPublisher outboxEventPublisher,
            MeterRegistry meterRegistry) {
        this.paymentRepository = paymentRepository;
        this.outboxEventPublisher = outboxEventPublisher;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Process a payment for an order.
     * This is triggered by Kafka events from the order service.
     */
    public PaymentResponse processPayment(ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        // 1. Transactional: Check idempotency and initialize payment
        Payment payment = initializePayment(command);
        if (!(payment.getStatus() instanceof com.ecommerce.payment.domain.model.PaymentStatus.Processing)) {
            return PaymentResponse.from(payment);
        }

        // 2. Non-Transactional: Long-running processing simulation
        try {
            simulatePaymentProcessing();

            // Random failure for testing
            if (random.nextDouble() < simulatedFailureRate) {
                throw new RuntimeException("Simulated payment failure");
            }

            // 3. Transactional: Complete payment and publish event
            payment = completePayment(payment.getId());
            log.info("Payment {} completed for order {}", payment.getId().value(), command.orderId());

        } catch (Exception e) {
            // 4. Transactional: Handle failure and publish event
            String errorCode = "PAYMENT_FAILED";
            String reason = e.getMessage() != null ? e.getMessage() : "Unknown error";
            payment = failPayment(payment.getId(), reason, errorCode);
            log.error("Payment {} failed for order {}: {}", payment.getId().value(), command.orderId(), reason);
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public Payment initializePayment(ProcessPaymentCommand command) {
        // Check if payment already exists for this order (idempotency)
        var existingPayment = paymentRepository.findByOrderId(command.orderId());
        if (existingPayment.isPresent()) {
            log.info("Payment already exists for order {}, returning existing", command.orderId());
            return existingPayment.get();
        }

        // Create new payment
        Payment payment = Payment.create(
                command.orderId(),
                command.customerId(),
                command.amount(),
                command.currency());

        // Start processing
        payment.startProcessing();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment completePayment(PaymentId id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id.value()));

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.complete(transactionId);
        payment = paymentRepository.save(payment);

        // Publish PaymentCompleted event
        var event = PaymentEvents.PaymentCompleted.create(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                transactionId);
        outboxEventPublisher.publish(AGGREGATE_TYPE, payment.getId().value(), event);

        // Metrics
        meterRegistry.counter("payment_success_total").increment();

        return payment;
    }

    @Transactional
    public Payment failPayment(PaymentId id, String reason, String errorCode) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id.value()));

        payment.fail(reason, errorCode);
        payment = paymentRepository.save(payment);

        // Publish PaymentFailed event
        var event = PaymentEvents.PaymentFailed.create(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getCustomerId(),
                reason,
                errorCode);
        outboxEventPublisher.publish(AGGREGATE_TYPE, payment.getId().value(), event);

        // Metrics
        meterRegistry.counter("payment_failed_total", "reason", reason).increment();

        return payment;
    }

    /**
     * Get payment by ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String id) {
        Payment payment = paymentRepository.findById(new PaymentId(id))
                .orElseThrow(() -> new NotFoundException("Payment", id));
        return PaymentResponse.from(payment);
    }

    /**
     * Get payment by order ID.
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment for order", orderId));
        return PaymentResponse.from(payment);
    }

    /**
     * Get all payments for a customer.
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * Refund a payment.
     */
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
            Thread.sleep(processingDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
