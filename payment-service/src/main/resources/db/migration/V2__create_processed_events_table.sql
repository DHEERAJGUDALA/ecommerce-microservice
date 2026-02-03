-- Idempotency Tracking Table
-- V2__create_processed_events_table.sql

CREATE TABLE IF NOT EXISTS processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for event type analytics
CREATE INDEX idx_processed_events_type ON processed_events(event_type, processed_at);
