package com.ally.blogapp.event;

public record CommentCreatedEvent(Long postId, Long commentId, Long userId) {}
