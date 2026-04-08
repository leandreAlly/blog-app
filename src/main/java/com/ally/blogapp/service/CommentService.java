package com.ally.blogapp.service;

import com.ally.blogapp.dao.CommentDao;
import com.ally.blogapp.dao.impl.CommentDaoImpl;
import com.ally.blogapp.model.Comment;

import java.util.List;
import java.util.Optional;

public class CommentService {

    private final CommentDao commentDao;

    public CommentService() {
        this.commentDao = new CommentDaoImpl();
    }

    public Comment create(Comment comment) {
        return commentDao.save(comment);
    }

    public Optional<Comment> findById(Long id) {
        return commentDao.findById(id);
    }

    public List<Comment> findByPostId(Long postId) {
        return commentDao.findByPostId(postId);
    }

    public List<Comment> findByUserId(Long userId) {
        return commentDao.findByUserId(userId);
    }

    public Comment update(Comment comment) {
        commentDao.findById(comment.getId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return commentDao.update(comment);
    }

    public void delete(Long id) {
        commentDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        commentDao.delete(id);
    }
}
