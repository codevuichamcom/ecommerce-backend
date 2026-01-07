package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;

import java.util.List;
import java.util.Optional;

/**
 * Payment repository interface (port).
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByCustomerId(String customerId);

    boolean existsByOrderId(String orderId);
}
