package com.ally.blogapp.dao.impl;

import com.ally.blogapp.dao.ReviewDao;
import com.ally.blogapp.model.Review;
import com.ally.blogapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDaoImpl implements ReviewDao {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setPostId(rs.getLong("post_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setContent(rs.getString("content"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return review;
    }

    @Override
    public Review save(Review review) {
        String sql = "INSERT INTO reviews (post_id, user_id, rating, content) VALUES (?, ?, ?, ?) RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, review.getPostId());
            stmt.setLong(2, review.getUserId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getContent());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving review", e);
        }
        return null;
    }

    @Override
    public Optional<Review> findById(Long id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding review by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Review> findByPostId(Long postId) {
        String sql = "SELECT * FROM reviews WHERE post_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reviews by post", e);
        }
        return reviews;
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reviews by user", e);
        }
        return reviews;
    }

    @Override
    public Optional<Review> findByPostIdAndUserId(Long postId, Long userId) {
        String sql = "SELECT * FROM reviews WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding review by post and user", e);
        }
        return Optional.empty();
    }

    @Override
    public double getAverageRatingByPostId(Long postId) {
        String sql = "SELECT COALESCE(AVG(rating), 0) AS avg_rating FROM reviews WHERE post_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting average rating", e);
        }
        return 0;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET rating = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getContent());
            stmt.setLong(3, review.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating review", e);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting review", e);
        }
    }
}
