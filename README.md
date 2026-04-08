# BlogApp - Smart Blogging Platform

A JavaFX desktop blogging application built with JDBC and PostgreSQL. The app supports two user roles — **Blogger** (content creator with a full dashboard) and **Reader** (clean blog-style browsing experience).

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [User Roles](#user-roles)
- [Screenshots Flow](#screenshots-flow)

---

## Features

- User registration and login with role selection (Blogger / Reader)
- Create, edit, publish, archive, and delete blog posts
- Tag management (comma-separated, auto-created)
- Comment on posts (add / delete own comments)
- Review posts with 1-5 star rating (one review per user per post)
- Search posts by keyword (case-insensitive across title and content)
- Role-based UI: Blogger gets a dashboard, Reader gets a blog-style feed
- Input validation and error handling throughout
- PostgreSQL database normalized to Third Normal Form (3NF)
- Indexed columns for optimized query performance

---

## Tech Stack

| Layer         | Technology                        |
|---------------|-----------------------------------|
| Language      | Java 25                           |
| UI Framework  | JavaFX 21 with FXML               |
| Database      | PostgreSQL 17                     |
| DB Access     | JDBC (parameterized queries)      |
| Build Tool    | Maven                             |
| IDE           | IntelliJ IDEA                     |

---

## Architecture

The application follows a **layered architecture** with clear separation of concerns:

```
Controller (JavaFX)  -->  Service (Business Logic)  -->  DAO (Data Access)  -->  PostgreSQL
```

| Layer        | Responsibility                                                  |
|--------------|-----------------------------------------------------------------|
| **Model**    | Entity classes (User, Post, Comment, Tag, Review) and enums     |
| **DAO**      | Interfaces + JDBC implementations for database CRUD operations  |
| **Service**  | Business rules, validation, and orchestration                   |
| **Controller** | JavaFX controllers handling UI events and user interaction     |
| **Util**     | Database connection singleton, scene/navigation manager         |

---

## Database Schema

The database contains 6 tables normalized to 3NF:

```
users
  |
  |--- posts (author_id FK)
  |       |
  |       |--- comments (post_id FK, user_id FK)
  |       |--- reviews  (post_id FK, user_id FK) [unique per user per post]
  |       |--- post_tags (post_id FK, tag_id FK) [many-to-many join table]
  |
  |--- tags
```

**Key constraints:**
- `users.username` and `users.email` are unique
- `reviews` has a unique constraint on `(post_id, user_id)` — one review per user per post
- `reviews.rating` has a CHECK constraint (1-5)
- All foreign keys use `ON DELETE CASCADE`
- Indexes on: `username`, `email`, `title`, `author_id`, `status`, `created_at`, `tag name`, `post_id`, `user_id`

The full SQL script is at [`docs/schema.sql`](docs/schema.sql) and the DBML diagram is at [`docs/blog-schema.dbml`](docs/blog-schema.dbml) (paste into [dbdiagram.io](https://dbdiagram.io) to visualize).

---

## Prerequisites

- **Java 25** (or compatible JDK)
- **Maven** (bundled with IntelliJ or install separately)
- **PostgreSQL 17** (install via Homebrew: `brew install postgresql@17`)

---

## Database Setup

1. Start PostgreSQL:

   ```bash
   brew services start postgresql@17
   ```

2. Create the database:

   ```bash
   createdb blogapp
   ```

3. Run the schema script:

   ```bash
   psql -d blogapp -f docs/schema.sql
   ```

4. Verify tables were created:

   ```bash
   psql -d blogapp -c "\dt"
   ```

   You should see: `users`, `posts`, `comments`, `tags`, `post_tags`, `reviews`.

5. Update database credentials if needed in:

   ```
   src/main/java/com/ally/blogapp/util/DatabaseConnection.java
   ```

   Default config uses your macOS username with no password.

---

## Running the Application

**From IntelliJ IDEA:**

1. Open the project in IntelliJ
2. Let Maven resolve dependencies
3. Run `Launcher.java` (contains the `main` method)

**From terminal:**

```bash
mvn clean javafx:run
```

---

## Project Structure

```
blog-app/
├── pom.xml
├── README.md
├── docs/
│   ├── schema.sql              # PostgreSQL DDL script
│   └── blog-schema.dbml        # DBML for dbdiagram.io
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/ally/blogapp/
    │       ├── Launcher.java               # Entry point
    │       ├── HelloApplication.java       # JavaFX Application class
    │       ├── model/
    │       │   ├── Role.java               # BLOGGER, READER
    │       │   ├── PostStatus.java         # DRAFT, PUBLISHED, ARCHIVED
    │       │   ├── User.java
    │       │   ├── Post.java
    │       │   ├── Comment.java
    │       │   ├── Tag.java
    │       │   └── Review.java
    │       ├── dao/
    │       │   ├── UserDao.java            # Interface
    │       │   ├── PostDao.java
    │       │   ├── CommentDao.java
    │       │   ├── TagDao.java
    │       │   ├── ReviewDao.java
    │       │   └── impl/
    │       │       ├── UserDaoImpl.java     # JDBC implementation
    │       │       ├── PostDaoImpl.java
    │       │       ├── CommentDaoImpl.java
    │       │       ├── TagDaoImpl.java
    │       │       └── ReviewDaoImpl.java
    │       ├── service/
    │       │   ├── UserService.java
    │       │   ├── PostService.java
    │       │   ├── CommentService.java
    │       │   ├── TagService.java
    │       │   └── ReviewService.java
    │       ├── controller/
    │       │   ├── LoginController.java
    │       │   ├── DashboardController.java
    │       │   ├── ReaderController.java
    │       │   ├── PostListController.java
    │       │   ├── PostEditorController.java
    │       │   └── PostDetailController.java
    │       └── util/
    │           ├── DatabaseConnection.java
    │           └── SceneManager.java
    └── resources/com/ally/blogapp/
        ├── login-view.fxml
        ├── dashboard-view.fxml
        ├── reader-view.fxml
        ├── post-list-view.fxml
        ├── post-editor-view.fxml
        └── post-detail-view.fxml
```

---

## User Roles

### Blogger

Bloggers get a **dashboard** with a sidebar for managing content:

- **All Posts** — browse all published posts
- **My Posts** — view own posts (drafts, published, archived)
- **Create Post** — write new posts with title, content, tags, and status
- **Edit / Delete** — modify or remove own posts
- **View** — read any post, leave comments and reviews

### Reader

Readers get a **blog-style feed** — clean, minimal, no dashboard:

- **Browse** — scroll through all published posts in a feed layout
- **Search** — find posts by keyword
- **Read** — click any post to read the full article
- **Comment** — add and delete own comments
- **Review** — rate posts 1-5 stars with optional text

---

## Screenshots Flow

```
Login Screen
    |
    ├── [BLOGGER] --> Dashboard (sidebar: All Posts / My Posts / Create Post / Logout)
    |                     |
    |                     ├── Post List (search, view, edit, delete)
    |                     ├── Post Editor (create / edit with tags & status)
    |                     └── Post Detail (full post + comments + reviews)
    |
    └── [READER]  --> Blog Feed (top nav: search, logout)
                          |
                          └── Article View (full post + comments + reviews, inline)
```
