-- Modify processed_events table to use VARCHAR for event_id
-- Drop the old table and recreate with correct schema
DROP TABLE IF EXISTS processed_events;

CREATE TABLE processed_events (
    event_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_processed_events_processed_at ON processed_events(processed_at);
