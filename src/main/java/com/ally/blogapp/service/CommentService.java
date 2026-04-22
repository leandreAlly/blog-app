package com.ally.blogapp.service;

import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Comment;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.User;
import com.ally.blogapp.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostService postService;

    public CommentService(CommentRepository commentRepository,
                          UserService userService,
                          PostService postService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.postService = postService;
    }

    @Transactional
    public Comment create(Long postId, Long userId, String content) {
        Post post = postService.findById(postId);
        User user = userService.findById(userId);
        return commentRepository.save(new Comment(post, user, content));
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    public Page<Comment> findByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    public long countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    @Transactional
    public Comment update(Long id, String content) {
        Comment comment = findById(id);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        commentRepository.deleteById(id);
    }
}
