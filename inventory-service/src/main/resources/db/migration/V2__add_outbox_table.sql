-- Flyway migration: Add Outbox table

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(50) PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_published ON outbox_events(published);

-- Idempotency table
CREATE TABLE IF NOT EXISTS processed_events (
    event_id VARCHAR(50) PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
