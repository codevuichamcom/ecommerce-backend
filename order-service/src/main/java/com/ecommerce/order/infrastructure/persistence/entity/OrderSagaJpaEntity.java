package com.ecommerce.order.infrastructure.persistence.entity;

import com.ecommerce.order.domain.saga.SagaState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "order_sagas")
@Getter
@Setter
@NoArgsConstructor
public class OrderSagaJpaEntity {

    @Id
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaState state;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
