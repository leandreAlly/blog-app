package com.ally.blogapp.event;

public record ReviewCreatedEvent(Long postId, Long reviewId, Long userId, int rating) {}
