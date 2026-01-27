package com.ecommerce.payment.application.service;

import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private com.ecommerce.payment.infrastructure.config.PaymentProperties paymentProperties;

    @Mock
    private com.ecommerce.payment.infrastructure.config.PaymentProperties.Processing processingProperties;

    @Mock
    private com.ecommerce.payment.application.service.InternalPaymentService internalPaymentService;

    @InjectMocks
    private PaymentService paymentService;

    private String orderId = "order_123";
    private String customerId = "cust_123";
    private BigDecimal amount = new BigDecimal("100.00");
    private String currency = "USD";

    @BeforeEach
    void setUp() {
        lenient().when(paymentProperties.getProcessing()).thenReturn(processingProperties);
        lenient().when(processingProperties.getDelayMs()).thenReturn(0L);
        lenient().when(processingProperties.getSimulatedFailureRate()).thenReturn(0.0);
    }

    @Test
    void processPayment_ShouldCompletePayment_WhenSuccessful() {
        // Given
        ProcessPaymentCommand command = new ProcessPaymentCommand(orderId, customerId, amount, currency);
        Payment payment = Payment.create(orderId, customerId, amount, currency);
        payment.startProcessing();

        when(internalPaymentService.initializePayment(any(ProcessPaymentCommand.class))).thenReturn(payment);
        when(internalPaymentService.completePayment(any(PaymentId.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.processPayment(command);

        // Then
        assertThat(response).isNotNull();
        verify(internalPaymentService).initializePayment(any(ProcessPaymentCommand.class));
        verify(internalPaymentService).completePayment(any(PaymentId.class));
    }

    @Test
    void processPayment_ShouldReturnExisting_WhenAlreadyProcessed() {
        // Given
        ProcessPaymentCommand command = new ProcessPaymentCommand(orderId, customerId, amount, currency);
        Payment existingPayment = Payment.create(orderId, customerId, amount, currency);
        existingPayment.startProcessing();
        existingPayment.complete("TXN-123");

        when(internalPaymentService.initializePayment(any(ProcessPaymentCommand.class))).thenReturn(existingPayment);

        // When
        PaymentResponse response = paymentService.processPayment(command);

        // Then
        assertThat(response.status()).isEqualTo("COMPLETED");
        verify(internalPaymentService, never()).completePayment(any());
    }

    @Test
    void processPayment_ShouldFail_WhenSimulationFails() {
        // Given
        when(processingProperties.getSimulatedFailureRate()).thenReturn(1.0);
        ProcessPaymentCommand command = new ProcessPaymentCommand(orderId, customerId, amount, currency);
        Payment payment = Payment.create(orderId, customerId, amount, currency);
        payment.startProcessing();

        when(internalPaymentService.initializePayment(any(ProcessPaymentCommand.class))).thenReturn(payment);
        when(internalPaymentService.failPayment(any(), anyString(), anyString())).thenReturn(payment);

        // When
        paymentService.processPayment(command);

        // Then
        verify(internalPaymentService).failPayment(any(), anyString(), eq("PAYMENT_FAILED"));
    }

    @Test
    void refundPayment_ShouldRefund_WhenPaymentExists() {
        // Given
        String paymentId = "pay_123";
        Payment payment = Payment.create(orderId, customerId, amount, currency);
        payment.startProcessing();
        payment.complete("TXN-123");

        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.refundPayment(paymentId, "Customer request");

        // Then
        assertThat(response.status()).isEqualTo("REFUNDED");
        verify(paymentRepository).save(payment);
        verify(outboxEventPublisher).publish(eq("Payment"), anyString(), any());
    }

    @Test
    void refundPayment_ShouldThrowNotFound_WhenPaymentDoesNotExist() {
        // Given
        when(paymentRepository.findById(any(PaymentId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment("invalid", "reason"))
                .isInstanceOf(NotFoundException.class);
    }
}
