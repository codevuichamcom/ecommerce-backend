package com.ecommerce.common.outbox;

import java.util.List;

/**
 * Repository interface for outbox operations.
 * Each service should implement this interface with their specific JPA
 * repository.
 */
public interface OutboxRepository {

    /**
     * Save an outbox message.
     */
    OutboxMessage save(OutboxMessage message);

    /**
     * Find all unpublished messages with a database lock to prevent concurrent
     * processing.
     * 
     * @param limit Maximum number of messages to return
     */
    List<OutboxMessage> findUnpublishedForUpdate(int limit);

    /**
     * Mark a message as published.
     */
    void markAsPublished(String messageId);

    /**
     * Delete old published messages (for cleanup).
     * 
     * @param olderThanDays Messages published more than this many days ago
     */
    void deletePublishedOlderThan(int olderThanDays);
}
