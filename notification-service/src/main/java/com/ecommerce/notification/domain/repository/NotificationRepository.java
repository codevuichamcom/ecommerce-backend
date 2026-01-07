package com.ecommerce.notification.domain.repository;

import com.ecommerce.notification.domain.model.Notification;
import com.ecommerce.notification.domain.model.NotificationId;

import java.util.List;
import java.util.Optional;

/**
 * Notification repository interface (port).
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    List<Notification> findByRecipientId(String recipientId);

    List<Notification> findPendingNotifications(int limit);
}
