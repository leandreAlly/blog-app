-- Performance indexes for paginated/sorted query patterns introduced in Spring Data JPA phase.

-- Published-feed: filter by status='PUBLISHED' then sort by published_at desc.
-- Partial index keeps it tiny since the vast majority of rows aren't queried in this path.
CREATE INDEX IF NOT EXISTS idx_posts_published_feed
    ON posts (published_at DESC)
    WHERE status = 'PUBLISHED';

-- Author archive: posts.findByAuthorIdOrderByCreatedAtDesc — covers the common author profile page.
CREATE INDEX IF NOT EXISTS idx_posts_author_created
    ON posts (author_id, created_at DESC);

-- Comments paginated per post, sorted by createdAt (default asc, optional desc).
CREATE INDEX IF NOT EXISTS idx_comments_post_created
    ON comments (post_id, created_at);

-- Reviews paginated per post, sorted by createdAt desc (controller default).
CREATE INDEX IF NOT EXISTS idx_reviews_post_created
    ON reviews (post_id, created_at DESC);

-- Top-rated reviews across all published posts (ReviewRepository.findTopRated).
CREATE INDEX IF NOT EXISTS idx_reviews_top_rated
    ON reviews (rating DESC, created_at DESC);
