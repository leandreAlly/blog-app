package com.ally.blogapp.dao.impl;

import com.ally.blogapp.dao.TagDao;
import com.ally.blogapp.model.Tag;
import com.ally.blogapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagDaoImpl implements TagDao {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Tag mapRow(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        tag.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return tag;
    }

    @Override
    public Tag save(Tag tag) {
        String sql = "INSERT INTO tags (name) VALUES (?) RETURNING *";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, tag.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving tag", e);
        }
        return null;
    }

    @Override
    public Optional<Tag> findById(Long id) {
        String sql = "SELECT * FROM tags WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tag by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT * FROM tags WHERE name = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tag by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tag> findAll() {
        String sql = "SELECT * FROM tags ORDER BY name";
        List<Tag> tags = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tags.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tags", e);
        }
        return tags;
    }

    @Override
    public List<Tag> findByPostId(Long postId) {
        String sql = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ? ORDER BY t.name";
        List<Tag> tags = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tags.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tags by post", e);
        }
        return tags;
    }

    @Override
    public void addTagToPost(Long postId, Long tagId) {
        String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, tagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding tag to post", e);
        }
    }

    @Override
    public void removeTagFromPost(Long postId, Long tagId) {
        String sql = "DELETE FROM post_tags WHERE post_id = ? AND tag_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, postId);
            stmt.setLong(2, tagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing tag from post", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting tag", e);
        }
    }
}
