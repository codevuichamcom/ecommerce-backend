package com.ecommerce.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base handler for idempotent event processing.
 * Ensures each event is processed exactly once.
 */
public class IdempotentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(IdempotentEventHandler.class);

    private final ProcessedEventRepository processedEventRepository;

    public IdempotentEventHandler(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Process an event idempotently.
     * If the event has already been processed, it will be skipped.
     *
     * @param eventId   Unique ID of the event
     * @param eventType Type of the event
     * @param handler   The actual event processing logic
     * @return true if the event was processed, false if it was already processed
     */
    @Transactional
    public boolean processIdempotently(String eventId, String eventType, Runnable handler) {
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("Event already processed (check), skipping: eventId={}, type={}", eventId, eventType);
            return false;
        }

        try {
            handler.run();
            processedEventRepository.save(ProcessedEvent.create(eventId, eventType));
            log.debug("Event processed successfully: eventId={}, type={}", eventId, eventType);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Event already processed (race condition), skipping: eventId={}, type={}", eventId, eventType);
            return false;
        } catch (Exception e) {
            log.error("Error processing event: eventId={}, type={}, error={}", eventId, eventType, e.getMessage());
            throw e;
        }
    }

    /**
     * Process an event idempotently with result.
     * If the event has already been processed, the default value will be returned.
     *
     * @param eventId      Unique ID of the event
     * @param eventType    Type of the event
     * @param handler      The actual event processing logic
     * @param defaultValue Value to return if event was already processed
     * @return Result of processing, or defaultValue if already processed
     */
    @Transactional
    public <T> T processIdempotently(String eventId, String eventType, java.util.function.Supplier<T> handler,
            T defaultValue) {
        if (processedEventRepository.existsByEventId(eventId)) {
            log.info("Event already processed, skipping: eventId={}, type={}", eventId, eventType);
            return defaultValue;
        }

        try {
            T result = handler.get();
            processedEventRepository.save(ProcessedEvent.create(eventId, eventType));
            log.debug("Event processed successfully: eventId={}, type={}", eventId, eventType);
            return result;
        } catch (Exception e) {
            log.error("Error processing event: eventId={}, type={}, error={}", eventId, eventType, e.getMessage());
            throw e;
        }
    }
}
