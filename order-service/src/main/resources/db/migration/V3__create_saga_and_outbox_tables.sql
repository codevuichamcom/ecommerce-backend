-- Create order saga table
CREATE TABLE order_sagas (
    order_id VARCHAR(50) PRIMARY KEY,
    state VARCHAR(50) NOT NULL,
    last_error TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

-- Create outbox table for reliable messaging
CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    published BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_outbox_status ON outbox_messages(published, created_at);

-- Create processed events table for idempotency
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
