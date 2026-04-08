package com.ally.blogapp.dao.impl;

import com.ally.blogapp.dao.PostDao;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStatus;
import com.ally.blogapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostDaoImpl implements PostDao {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setAuthorId(rs.getLong("author_id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setStatus(PostStatus.valueOf(rs.getString("status")));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            post.setPublishedAt(publishedAt.toLocalDateTime());
        }
        return post;
    }

    @Override
    public Post save(Post post) {
        String sql = "INSERT INTO posts (author_id, title, content, status) VALUES (?, ?, ?, ?) RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, post.getAuthorId());
            stmt.setString(2, post.getTitle());
            stmt.setString(3, post.getContent());
            stmt.setString(4, post.getStatus().name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving post", e);
        }
        return null;
    }

    @Override
    public Optional<Post> findById(Long id) {
        String sql = "SELECT * FROM posts WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding post by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Post> findByAuthorId(Long authorId) {
        String sql = "SELECT * FROM posts WHERE author_id = ? ORDER BY created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, authorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding posts by author", e);
        }
        return posts;
    }

    @Override
    public List<Post> findByStatus(PostStatus status) {
        String sql = "SELECT * FROM posts WHERE status = ? ORDER BY created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding posts by status", e);
        }
        return posts;
    }

    @Override
    public List<Post> findAll() {
        String sql = "SELECT * FROM posts ORDER BY created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                posts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all posts", e);
        }
        return posts;
    }

    @Override
    public List<Post> searchByKeyword(String keyword) {
        String sql = "SELECT * FROM posts WHERE LOWER(title) LIKE ? OR LOWER(content) LIKE ? ORDER BY created_at DESC";
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching posts", e);
        }
        return posts;
    }

    @Override
    public Post update(Post post) {
        String sql = "UPDATE posts SET title = ?, content = ?, status = ?, published_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setString(3, post.getStatus().name());
            if (post.getPublishedAt() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(post.getPublishedAt()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            stmt.setLong(5, post.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating post", e);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting post", e);
        }
    }
}
