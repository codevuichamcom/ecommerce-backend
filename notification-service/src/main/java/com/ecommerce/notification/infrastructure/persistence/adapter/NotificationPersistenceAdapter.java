package com.ecommerce.notification.infrastructure.persistence.adapter;

import com.ecommerce.notification.domain.model.Notification;
import com.ecommerce.notification.domain.model.NotificationId;
import com.ecommerce.notification.domain.repository.NotificationRepository;
import com.ecommerce.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing NotificationRepository using JPA.
 */
@Component
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationPersistenceAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @SuppressWarnings("null")
    public Notification save(Notification notification) {
        var entity = NotificationMapper.toEntity(notification);
        var savedEntity = jpaRepository.save(entity);
        return NotificationMapper.toDomain(savedEntity);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value())
                .map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findPendingNotifications(int limit) {
        return jpaRepository.findPendingNotifications(limit).stream()
                .map(NotificationMapper::toDomain)
                .toList();
    }
}
