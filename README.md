# BlogApp — Spring Boot Blogging Platform

A RESTful + GraphQL blogging platform built with **Spring Boot 3.3**, **Spring Data JPA**, **Spring AOP**, **Spring GraphQL**, and **PostgreSQL**.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Configuration Profiles](#configuration-profiles)
- [REST API Reference](#rest-api-reference)
- [GraphQL API](#graphql-api)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [AOP: Logging & Performance Monitoring](#aop-logging--performance-monitoring)
- [Caching](#caching)
- [Database Indexes](#database-indexes)
- [Transactions](#transactions)
- [Running Tests](#running-tests)
- [Performance Report](#performance-report)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.5 |
| Language | Java 21 |
| Database | PostgreSQL 14+ |
| ORM | Spring Data JPA / Hibernate 6 |
| Validation | Jakarta Bean Validation |
| API Docs | Springdoc OpenAPI 2.6 (Swagger UI) |
| GraphQL | Spring for GraphQL |
| AOP | Spring AOP (AspectJ) |
| Cache | Spring Cache + Caffeine |
| Migrations | Flyway |
| Testing | JUnit 5, Mockito, Spring Boot Test |

---

## Architecture

```
Controller (REST + GraphQL) → Service → Repository (JPA) → PostgreSQL
```

Cross-cutting concerns (logging, performance) are applied via AOP aspects on the service layer.

---

## Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 14+

---

## Database Setup

1. Create the database:
```sql
CREATE DATABASE blogapp;
```

2. Run the schema script:
```bash
psql -U <your_user> -d blogapp -f docs/schema.sql
```

For tests, create a separate database:
```sql
CREATE DATABASE blogapp_test;
```

---

## Running the Application

```bash
# Dev profile (default)
./mvnw spring-boot:run

# Explicit profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

The server starts on **http://localhost:8080**.

---

## Configuration Profiles

| Profile | Database | GraphiQL | Log level |
|---|---|---|---|
| `dev` | localhost:5432/blogapp | Enabled | DEBUG |
| `test` | localhost:5432/blogapp_test | Disabled | WARN |
| `prod` | `${DB_URL}` env var | Disabled | WARN |

### Production environment variables

```
DB_URL=jdbc:postgresql://host:5432/blogapp
DB_USERNAME=...
DB_PASSWORD=...
PORT=8080
```

---

## REST API Reference

Base URL: `http://localhost:8080/api`

### Users

| Method | Endpoint | Description |
|---|---|---|
| GET | `/users` | List all users |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Register a user |
| PUT | `/users/{id}` | Update profile (bio, profileImage) |
| DELETE | `/users/{id}` | Delete user |

### Posts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/posts` | List posts (paginated, filterable) |
| GET | `/posts?search=keyword` | Full-text search |
| GET | `/posts?status=PUBLISHED` | Filter by status |
| GET | `/posts?tagId=1` | Filter published posts by tag |
| GET | `/posts?page=0&size=10&sort=createdAt&direction=desc` | Pagination & sorting |
| GET | `/posts/{id}` | Get post by ID |
| GET | `/posts/author/{authorId}` | Get posts by author |
| GET | `/posts/author/{authorId}/stats` | Author post analytics |
| GET | `/posts/trending?limit=10` | Trending posts (native query, last 7-day activity) |
| POST | `/posts` | Create post (DRAFT) |
| PUT | `/posts/{id}` | Update post |
| POST | `/posts/{id}/publish` | Publish post |
| POST | `/posts/{id}/archive` | Archive post |
| POST | `/posts/{postId}/tags/{tagId}` | Add tag to post |
| DELETE | `/posts/{postId}/tags/{tagId}` | Remove tag from post |
| DELETE | `/posts/{id}` | Delete post |

### Comments (`/posts/{postId}/comments`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/posts/{postId}/comments?page=0&size=10&sort=createdAt&direction=asc` | List comments on post (paginated) |
| GET | `/posts/{postId}/comments/count` | Total comments on post |
| POST | `/posts/{postId}/comments` | Add comment |
| PUT | `/posts/{postId}/comments/{id}` | Update comment |
| DELETE | `/posts/{postId}/comments/{id}` | Delete comment |

### Tags

| Method | Endpoint | Description |
|---|---|---|
| GET | `/tags?page=0&size=20&sort=name&direction=asc` | List tags (paginated) |
| GET | `/tags/search?q=spring` | Case-insensitive name search |
| GET | `/tags/popular?limit=10` | Most-used tags (cached) |
| GET | `/tags/{id}` | Get tag by ID |
| POST | `/tags` | Create tag |
| DELETE | `/tags/{id}` | Delete tag |

### Reviews (`/posts/{postId}/reviews`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/posts/{postId}/reviews?page=0&size=10&sort=createdAt&direction=desc` | List reviews on post (paginated) |
| GET | `/posts/{postId}/reviews/average` | Average rating |
| POST | `/posts/{postId}/reviews` | Add review (1–5 stars) |
| PUT | `/posts/{postId}/reviews/{id}` | Update review |
| DELETE | `/posts/{postId}/reviews/{id}` | Delete review |

### Response format

All endpoints return a consistent envelope:

```json
{
  "status": 200,
  "message": "Posts retrieved",
  "data": { ... }
}
```

---

## GraphQL API

GraphiQL playground (dev only): **http://localhost:8080/graphiql**

### Sample Queries

```graphql
# Get all published posts paginated
query {
  publishedPosts(page: 0, size: 5) {
    content { id title author { username } status }
    pageInfo { totalElements totalPages currentPage }
  }
}

# Full-text search
query {
  searchPosts(keyword: "spring boot") {
    content { id title }
  }
}

# Author analytics
query {
  postStats(authorId: "1") {
    title status commentCount reviewCount avgRating
  }
}
```

### Sample Mutations

```graphql
mutation {
  createUser(username: "alice", email: "alice@example.com", password: "secret", role: "BLOGGER") {
    id username role
  }
}

mutation {
  createPost(authorId: "1", title: "My First Post", content: "Hello world", tags: ["spring", "java"]) {
    id title status tags { name }
  }
}

mutation {
  publishPost(id: "1") { id status publishedAt }
}

mutation {
  createReview(postId: "1", userId: "2", rating: 5, content: "Excellent!") {
    id rating username
  }
}
```

---

## API Documentation (Swagger UI)

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

---

## AOP: Logging & Performance Monitoring

Two aspects are applied to all service-layer methods:

### LoggingAspect

- `@Before` — logs method name and arguments on entry
- `@AfterReturning` — logs return value on exit
- `@AfterThrowing` — logs exception message on error

### PerformanceAspect

- `@Around` — measures wall-clock execution time in ms
- Logs at `DEBUG` for normal calls
- Logs at `WARN` if execution exceeds **500ms**

Example log output (dev profile):

```
DEBUG >> com.ally.blogapp.service.PostService.create() args=[1, My Post, ...]
DEBUG PERF [12ms] com.ally.blogapp.service.PostService.create
DEBUG << com.ally.blogapp.service.PostService.create() result=Post{id=5, ...}

WARN  SLOW [623ms] com.ally.blogapp.service.PostService.search
```

---

## Caching

In-memory cache backed by **Caffeine** (Spring Cache abstraction). Configured in `config/CacheConfig.java`.

| Cache name | Used by | Max size | TTL |
|---|---|---|---|
| `posts` | `PostService.findById` | 500 | 5 min |
| `users` | `UserService.findById` | 500 | 5 min |
| `tags` | `TagService.findById` / `findByName` | 200 | 30 min |
| `popularTags` | `TagService.findPopular` | 50 | 10 min |
| `topAuthors` | (reserved) | 50 | 10 min |

**Eviction policy:**
- Per-entity caches (`posts`, `users`) evict by id on `update`/`delete`/`publish`/`archive`/`addTag`/`removeTag`
- `tags` and `popularTags` use `@Caching(evict = {...allEntries = true})` on `create`/`delete` since aggregate ordering changes whenever any tag is added or removed

Statistics are recorded (`Caffeine.recordStats()`) so hit/miss ratios can be inspected via Actuator if `spring-boot-starter-actuator` is added.

---

## Database Indexes

Schema migrations are managed by **Flyway** under `src/main/resources/db/migration/`.

- **`V1__initial_schema.sql`** — base tables and basic indexes (FKs, unique columns, search vector)
- **`V2__performance_indexes.sql`** — composite/partial indexes targeting paginated query patterns:
  - `idx_posts_published_feed` — partial `(published_at DESC) WHERE status='PUBLISHED'`
  - `idx_posts_author_created` — composite `(author_id, created_at DESC)` for author archives
  - `idx_comments_post_created` — composite `(post_id, created_at)` for paginated comments
  - `idx_reviews_post_created` — composite `(post_id, created_at DESC)` for paginated reviews
  - `idx_reviews_top_rated` — composite `(rating DESC, created_at DESC)` for top-rated lookups

---

## Transactions

Every service is annotated `@Transactional(readOnly = true)` at the class level so reads run in a Hibernate read-only transaction (no dirty-checking on flush). Write methods override with explicit settings:

```java
@Transactional(propagation = Propagation.REQUIRED,
               isolation = Isolation.READ_COMMITTED,
               rollbackFor = Exception.class)
```

**Stricter isolation** (`REPEATABLE_READ`) is used on flows that check uniqueness before insert:
- `UserService.register` — guards against duplicate-username races
- `ReviewService.create` — guards against duplicate `(post, user)` review races

`TagService.findOrCreate` uses `Propagation.REQUIRES_NEW` so a tag-collision failure during post creation doesn't poison the parent transaction.

Rollback behavior is verified by `TransactionRollbackTest` (see [Running Tests](#running-tests)).

---

## Running Tests

```bash
./mvnw test
```

- **Unit tests** (`*ServiceTest`) use Mockito to mock repositories — fast, no DB needed.
- **Integration tests** (`TransactionRollbackTest`) use `@SpringBootTest` against the `blogapp_test` Postgres database to verify real transactional behavior. Start Postgres and create the test DB before running.

---

## Performance Report

A detailed methodology and pre/post-optimization comparison lives in **[`docs/PERFORMANCE.md`](docs/PERFORMANCE.md)**. Highlights:

- **Caching** — repeated reads of a single post/user/tag bypass the database after the first request and serve from Caffeine in microseconds.
- **Composite indexes** — paginated comment/review listings sorted by `created_at` no longer require a full sort; the index supports the `ORDER BY` directly.
- **Partial index on published posts** — the public feed query reads only the published-feed index instead of scanning the full posts table.
- **Native trending query** — leverages the same `(post_id, created_at)` indexes already added for comments and reviews so the activity join stays cheap.
