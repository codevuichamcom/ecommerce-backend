-- Create retry tasks table for fallback mechanism
CREATE TABLE retry_tasks (
    id UUID PRIMARY KEY,
    task_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    max_retries INTEGER NOT NULL DEFAULT 3,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error TEXT
);

CREATE INDEX idx_retry_tasks_status_next_retry ON retry_tasks(status, next_retry_at);
