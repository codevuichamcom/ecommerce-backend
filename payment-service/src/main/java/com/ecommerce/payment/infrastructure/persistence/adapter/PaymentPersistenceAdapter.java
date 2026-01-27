package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentId;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.infrastructure.persistence.mapper.PaymentMapper;
import com.ecommerce.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing PaymentRepository using JPA.
 */
@Component
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper paymentMapper;

    public PaymentPersistenceAdapter(PaymentJpaRepository jpaRepository, PaymentMapper paymentMapper) {
        this.jpaRepository = jpaRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @SuppressWarnings("null")
    public Payment save(Payment payment) {
        var entity = paymentMapper.toEntity(payment);
        return paymentMapper.toDomainEntity(jpaRepository.save(entity));
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(java.util.Objects.requireNonNull(id.value()))
                .map(paymentMapper::toDomainEntity);
    }

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId)
                .map(paymentMapper::toDomainEntity);
    }

    @Override
    public List<Payment> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(paymentMapper::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByOrderId(String orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}
