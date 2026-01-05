package com.ecommerce.common.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Aggregate Roots in Domain-Driven Design.
 * Aggregates are consistency boundaries that manage domain events.
 */
public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Returns the unique identifier of this aggregate.
     */
    public abstract ID getId();

    /**
     * Returns the creation timestamp.
     */
    public abstract Instant getCreatedAt();

    /**
     * Registers a domain event to be published after transaction commits.
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Clears and returns all registered domain events.
     * Called by infrastructure after publishing events.
     */
    public List<DomainEvent> clearDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns registered domain events without clearing.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
