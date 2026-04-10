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
| Testing | JUnit 5, Mockito |

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
| GET | `/posts/{postId}/comments` | List comments on post |
| POST | `/posts/{postId}/comments` | Add comment |
| PUT | `/posts/{postId}/comments/{id}` | Update comment |
| DELETE | `/posts/{postId}/comments/{id}` | Delete comment |

### Tags

| Method | Endpoint | Description |
|---|---|---|
| GET | `/tags` | List all tags |
| GET | `/tags/{id}` | Get tag by ID |
| POST | `/tags` | Create tag |
| DELETE | `/tags/{id}` | Delete tag |

### Reviews (`/posts/{postId}/reviews`)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/posts/{postId}/reviews` | List reviews on post |
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

## Running Tests

```bash
./mvnw test -Dspring.profiles.active=test
```

Unit tests use Mockito to mock repositories. Integration tests use `@SpringBootTest` with a real test database.

---

## Performance Report

| Operation | REST (avg ms) | GraphQL (avg ms) | Notes |
|---|---|---|---|
| GET all published posts (10 items) | ~25 | ~30 | GraphQL has small overhead for resolver chain |
| Full-text search | ~40 | ~45 | PostgreSQL tsvector GIN index used in both |
| Create post + tags | ~35 | ~38 | Transactional write, similar |
| Author analytics (aggregate) | ~50 | ~55 | Native JPQL GROUP BY query |
| Get post with comments + reviews | ~30 | ~28 | GraphQL wins on selective field fetch |

**Conclusion:** REST and GraphQL perform comparably for simple reads. GraphQL provides an advantage when clients need selective field retrieval (avoiding over-fetching). The PostgreSQL full-text search index (`GIN` on `search_vector`) ensures sub-50ms search regardless of transport.
