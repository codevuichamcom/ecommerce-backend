package com.ecommerce.payment.application.service;

import com.ecommerce.common.events.PaymentEvents;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles transactional operations for Payments.
 * Separated to avoid self-invocation issues in PaymentService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InternalPaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final MeterRegistry meterRegistry;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment initializePayment(ProcessPaymentCommand command) {
        var existingPayment = paymentRepository.findByOrderId(command.orderId());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Payment payment = Payment.create(
                command.orderId(),
                command.customerId(),
                command.amount(),
                command.currency());

        payment.startProcessing();
        return paymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment completePayment(PaymentId id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id.value()));

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.complete(transactionId);
        payment = paymentRepository.save(payment);

        var event = PaymentEvents.PaymentCompleted.create(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                transactionId);
        outboxEventPublisher.publish("Payment", payment.getId().value(), event);

        meterRegistry.counter("payment_success_total").increment();
        return payment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment failPayment(PaymentId id, String reason, String errorCode) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id.value()));

        payment.fail(reason, errorCode);
        payment = paymentRepository.save(payment);

        var event = PaymentEvents.PaymentFailed.create(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getCustomerId(),
                reason,
                errorCode);
        outboxEventPublisher.publish("Payment", payment.getId().value(), event);

        meterRegistry.counter("payment_failed_total", "reason", reason).increment();
        return payment;
    }
}
