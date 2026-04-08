package com.ally.blogapp.dao.impl;

import com.ally.blogapp.dao.CommentDao;
import com.ally.blogapp.model.Comment;
import com.ally.blogapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentDaoImpl implements CommentDao {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setPostId(rs.getLong("post_id"));
        comment.setUserId(rs.getLong("user_id"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        comment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return comment;
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?) RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, comment.getPostId());
            stmt.setLong(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving comment", e);
        }
        return null;
    }

    @Override
    public Optional<Comment> findById(Long id) {
        String sql = "SELECT * FROM comments WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding comment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at DESC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding comments by post", e);
        }
        return comments;
    }

    @Override
    public List<Comment> findByUserId(Long userId) {
        String sql = "SELECT * FROM comments WHERE user_id = ? ORDER BY created_at DESC";
        List<Comment> comments = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                comments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding comments by user", e);
        }
        return comments;
    }

    @Override
    public Comment update(Comment comment) {
        String sql = "UPDATE comments SET content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, comment.getContent());
            stmt.setLong(2, comment.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating comment", e);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting comment", e);
        }
    }
}
