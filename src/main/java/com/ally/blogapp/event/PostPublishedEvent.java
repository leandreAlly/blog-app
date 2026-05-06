package com.ally.blogapp.event;

public record PostPublishedEvent(Long postId, Long authorId, String title) {}
