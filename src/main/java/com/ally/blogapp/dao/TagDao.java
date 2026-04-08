package com.ally.blogapp.dao;

import com.ally.blogapp.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagDao {
    Tag save(Tag tag);
    Optional<Tag> findById(Long id);
    Optional<Tag> findByName(String name);
    List<Tag> findAll();
    List<Tag> findByPostId(Long postId);
    void addTagToPost(Long postId, Long tagId);
    void removeTagFromPost(Long postId, Long tagId);
    void delete(Long id);
}
