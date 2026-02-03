-- Transactional Outbox Pattern Table
-- V2__create_outbox_events_table.sql

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    fail_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Index for efficient polling of PENDING events
-- This index supports the WHERE clause in the poller query
CREATE INDEX idx_outbox_pending ON outbox_events(status, retry_count, created_at)
    WHERE status = 'PENDING';

-- Index for aggregate tracking
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_id, aggregate_type);

-- Index for event type analytics
CREATE INDEX idx_outbox_event_type ON outbox_events(event_type, created_at);
