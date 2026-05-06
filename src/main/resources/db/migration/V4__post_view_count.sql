-- Per-post view counter. Updates are batched in memory by ViewCountService
-- (ConcurrentHashMap<Long, LongAdder>) and flushed periodically, so this
-- column receives one UPDATE per flush window rather than one per request.
ALTER TABLE posts ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;
