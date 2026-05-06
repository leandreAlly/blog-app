package com.ally.blogapp.dto.response;

import com.ally.blogapp.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        Long authorId,
        String authorUsername,
        String title,
        String content,
        String status,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        long viewCount
) {
    public static PostResponse from(Post post) {
        return from(post, 0L);
    }

    public static PostResponse from(Post post, long pendingViews) {
        List<String> tagNames = post.getTags().stream()
                .map(t -> t.getName())
                .sorted()
                .toList();
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getTitle(),
                post.getContent(),
                post.getStatus().name(),
                tagNames,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishedAt(),
                post.getViewCount() + pendingViews
        );
    }
}
