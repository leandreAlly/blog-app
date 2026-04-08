package com.ally.blogapp.dao;

import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStatus;

import java.util.List;
import java.util.Optional;

public interface PostDao {
    Post save(Post post);
    Optional<Post> findById(Long id);
    List<Post> findByAuthorId(Long authorId);
    List<Post> findByStatus(PostStatus status);
    List<Post> findAll();
    List<Post> searchByKeyword(String keyword);
    Post update(Post post);
    void delete(Long id);
}
