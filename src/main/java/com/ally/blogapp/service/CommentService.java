package com.ally.blogapp.service;

import com.ally.blogapp.event.CommentCreatedEvent;
import com.ally.blogapp.exception.ResourceNotFoundException;
import com.ally.blogapp.model.Comment;
import com.ally.blogapp.model.Post;
import com.ally.blogapp.model.User;
import com.ally.blogapp.repository.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostService postService;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(CommentRepository commentRepository,
                          UserService userService,
                          PostService postService,
                          ApplicationEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.postService = postService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Comment create(Long postId, Long userId, String content) {
        Post post = postService.findById(postId);
        User user = userService.findById(userId);
        Comment saved = commentRepository.save(new Comment(post, user, content));
        eventPublisher.publishEvent(new CommentCreatedEvent(postId, saved.getId(), userId));
        return saved;
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

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public Comment update(Long id, String content) {
        Comment comment = findById(id);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void delete(Long id) {
        findById(id);
        commentRepository.deleteById(id);
    }
}
