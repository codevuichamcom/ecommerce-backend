package com.ecommerce.notification.infrastructure.persistence.repository;

import com.ecommerce.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for notifications.
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, String> {

    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.status = 'PENDING' ORDER BY n.createdAt ASC LIMIT :limit")
    List<NotificationJpaEntity> findPendingNotifications(@Param("limit") int limit);
}
