package com.ally.blogapp.dao;

import com.ally.blogapp.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentDao {
    Comment save(Comment comment);
    Optional<Comment> findById(Long id);
    List<Comment> findByPostId(Long postId);
    List<Comment> findByUserId(Long userId);
    Comment update(Comment comment);
    void delete(Long id);
}
