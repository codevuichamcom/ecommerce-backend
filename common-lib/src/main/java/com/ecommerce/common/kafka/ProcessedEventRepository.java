package com.ecommerce.common.kafka;

/**
 * Repository interface for processed event tracking.
 * Each service should implement this with their specific JPA repository.
 */
public interface ProcessedEventRepository {

    /**
     * Check if an event has already been processed.
     */
    boolean existsByEventId(String eventId);

    /**
     * Save a processed event record.
     */
    ProcessedEvent save(ProcessedEvent event);

    /**
     * Delete old processed events (for cleanup).
     * 
     * @param olderThanDays Events processed more than this many days ago
     */
    void deleteOlderThan(int olderThanDays);
}
