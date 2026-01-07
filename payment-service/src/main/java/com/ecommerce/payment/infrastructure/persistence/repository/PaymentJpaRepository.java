package com.ecommerce.payment.infrastructure.persistence.repository;

import com.ecommerce.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for payments.
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, String> {

    Optional<PaymentJpaEntity> findByOrderId(String orderId);

    List<PaymentJpaEntity> findByCustomerId(String customerId);

    boolean existsByOrderId(String orderId);
}
