package com.ally.blogapp.service;

import com.ally.blogapp.dao.PostDao;
import com.ally.blogapp.dao.impl.PostDaoImpl;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PostService {

    private final PostDao postDao;

    public PostService() {
        this.postDao = new PostDaoImpl();
    }

    public Post create(Post post) {
        post.setStatus(PostStatus.DRAFT);
        return postDao.save(post);
    }

    public Optional<Post> findById(Long id) {
        return postDao.findById(id);
    }

    public List<Post> findByAuthorId(Long authorId) {
        return postDao.findByAuthorId(authorId);
    }

    public List<Post> findPublished() {
        return postDao.findByStatus(PostStatus.PUBLISHED);
    }

    public List<Post> findAll() {
        return postDao.findAll();
    }

    public List<Post> search(String keyword) {
        return postDao.searchByKeyword(keyword);
    }

    public Post publish(Long postId) {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        return postDao.update(post);
    }

    public Post archive(Long postId) {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setStatus(PostStatus.ARCHIVED);
        return postDao.update(post);
    }

    public Post update(Post post) {
        postDao.findById(post.getId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return postDao.update(post);
    }

    public void delete(Long id) {
        postDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        postDao.delete(id);
    }
}
