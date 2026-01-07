-- Notification Service Database Schema
-- V1__create_notification_tables.sql

-- Notifications table
CREATE TABLE notifications (
    id VARCHAR(50) PRIMARY KEY,
    recipient_id VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255),
    type VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    metadata TEXT,
    status VARCHAR(20) NOT NULL,
    message_id VARCHAR(100),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for common queries
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Processed events table for idempotency
CREATE TABLE processed_events (
    event_id VARCHAR(50) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index for cleanup
CREATE INDEX idx_processed_events_processed_at ON processed_events(processed_at);
