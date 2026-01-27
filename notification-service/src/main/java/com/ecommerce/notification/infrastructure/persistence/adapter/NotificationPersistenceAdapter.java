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
    private final com.ecommerce.notification.infrastructure.persistence.mapper.NotificationMapper notificationMapper;

    public NotificationPersistenceAdapter(
            NotificationJpaRepository jpaRepository,
            com.ecommerce.notification.infrastructure.persistence.mapper.NotificationMapper notificationMapper) {
        this.jpaRepository = jpaRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @SuppressWarnings("null")
    public Notification save(Notification notification) {
        var entity = notificationMapper.toEntity(notification);
        var savedEntity = jpaRepository.save(entity);
        return notificationMapper.toDomainEntity(savedEntity);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value())
                .map(notificationMapper::toDomainEntity);
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return jpaRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(notificationMapper::toDomainEntity)
                .toList();
    }

    @Override
    public List<Notification> findPendingNotifications(int limit) {
        return jpaRepository.findPendingNotifications(limit).stream()
                .map(notificationMapper::toDomainEntity)
                .toList();
    }
}
