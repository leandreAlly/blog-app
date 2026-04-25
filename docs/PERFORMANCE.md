# Performance Report — Spring Data JPA Phase

This report documents the optimizations applied during the Spring Data JPA phase of the Blogging Platform and the methodology used to evaluate them.

## What changed

| Area | Before | After |
|---|---|---|
| Repository methods | Mixed; only `PostRepository` had derived/JPQL queries | All five entities have derived methods, JPQL queries, and `count*` methods |
| Pagination | Only `GET /api/posts` was paginated | Comments, reviews, and tags now accept `page`, `size`, `sort`, `direction` |
| Transactions | Single `@Transactional` per write method, default settings | Class-level `readOnly = true`; writes specify `propagation`, `isolation`, `rollbackFor`; uniqueness flows use `REPEATABLE_READ` |
| Caching | None | Spring Cache + Caffeine, five named caches with TTL and size limits |
| Indexes | Single-column indexes on FKs and search vector | Added composite/partial indexes for `published_at`, `(author_id, created_at)`, `(post_id, created_at)`, `(rating, created_at)` |
| Analytics | JPQL stats per author | New native `findTrending` query for last-7-day post engagement |

## Methodology

Because this is an academic project without production traffic, performance was evaluated qualitatively by inspecting:

1. **Spring Boot SQL log** — running the app with `spring.jpa.show-sql=true` and exercising endpoints from Postman, then reading the generated SQL to confirm the planner uses the expected indexes.
2. **`EXPLAIN ANALYZE`** in `psql` — the migrations declare which indexes should serve which query patterns; running `EXPLAIN ANALYZE` against representative queries confirmed index scans rather than sequential scans.
3. **Cache behavior** — calling the same `GET /api/posts/{id}` twice in a row and observing that the second call produces no SQL in the log (the cache served it).
4. **Transactional rollback** — verified by `TransactionRollbackTest`, which seeds data, triggers a known failure, and asserts the table state is unchanged.

## Caching impact

The cache is Caffeine in-memory with `recordStats()` enabled.

| Path | First request | Second request (warm cache) |
|---|---|---|
| `GET /api/posts/{id}` | One `SELECT` against `posts` (PK lookup) | Zero SQL — served from `posts` cache |
| `GET /api/tags/popular?limit=10` | JPQL aggregation `GROUP BY t ORDER BY COUNT(p) DESC` | Zero SQL until eviction or 10-min TTL |
| `GET /api/users/{id}` | One `SELECT` against `users` | Zero SQL |

The most meaningful win is `popularTags`: the underlying query joins `tags` × `post_tags` × `posts` and groups, which is more expensive than a PK lookup. Caching collapses that to a single map lookup for repeat callers in the 10-minute window.

**Eviction correctness** — every write that could shift the result set evicts the relevant cache:
- `PostService.update`/`delete`/`publish`/`archive` evict `posts` by id
- `TagService.create`/`delete` clear both `tags` and `popularTags` (`allEntries = true`) because aggregate ordering shifts when any tag is added or removed
- `UserService.update`/`delete` evict `users` by id

## Index impact

`V2__performance_indexes.sql` adds composite and partial indexes:

| Index | Query pattern | Why it helps |
|---|---|---|
| `idx_posts_published_feed` | `WHERE status='PUBLISHED' ORDER BY published_at DESC` | Partial index — skips drafts/archived rows entirely; pagination becomes a backwards index scan with no sort step |
| `idx_posts_author_created` | `WHERE author_id=? ORDER BY created_at DESC` | Author archive page — single index scan; pre-existing `idx_posts_author_id` could not satisfy the sort |
| `idx_comments_post_created` | `WHERE post_id=? ORDER BY created_at` | Paginated comments per post — `LIMIT/OFFSET` pagination uses the index for both filter and order |
| `idx_reviews_post_created` | `WHERE post_id=? ORDER BY created_at DESC` | Paginated reviews — same pattern |
| `idx_reviews_top_rated` | `ORDER BY rating DESC, created_at DESC` | `findTopRated` — cross-post leaderboard avoids a full sort |

To validate any of these in your own environment:

```bash
psql blogapp -c "EXPLAIN ANALYZE
  SELECT * FROM posts WHERE status='PUBLISHED'
  ORDER BY published_at DESC LIMIT 10;"
```

Look for `Index Scan using idx_posts_published_feed` and confirm there is no `Sort` node above it.

## Transaction tuning

The class-level `@Transactional(readOnly = true)` is a small but consistent saving on every read: Hibernate skips the dirty-check step at flush time and the JDBC driver may use a lighter transaction mode where supported.

`Isolation.REPEATABLE_READ` on `register` and `createReview` adds a phantom-read guarantee that closes a small race window where two concurrent requests could both pass a uniqueness check and both insert. The trade-off — slightly higher locking — is acceptable here because both operations are user-initiated and not on a hot path.

`Propagation.REQUIRES_NEW` on `TagService.findOrCreate` isolates per-tag failures so creating a post with five tags doesn't lose all five if one tag's insert collides with a concurrent request.

## What's not measured

This report does not include:

- Concrete millisecond benchmarks at scale — that would require seeding the DB with realistic volume (>100k posts) and running JMH, which is out of scope.
- Cache hit ratios under load — those would surface from Actuator + Caffeine `recordStats()` if the project added `spring-boot-starter-actuator`.
- Distributed cache concerns (Redis, multi-instance invalidation) — Caffeine is in-process and intentionally simple for a single-node deployment.

## Reproducing the comparison

1. Check out the parent commit (`8ef4595` — pre-Spring-Data-JPA-phase).
2. Run `EXPLAIN ANALYZE` on a paginated comments query and note the plan (likely `Seq Scan` + `Sort`).
3. Check out the head of `ft-springboot-data-jpa`.
4. Run `./mvnw flyway:migrate` (or just start the app — Flyway runs at boot).
5. Re-run `EXPLAIN ANALYZE` — observe `Index Scan using idx_comments_post_created` with no separate `Sort` node.
